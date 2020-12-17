import { Controller, Post, Body, UsePipes, ValidationPipe, HttpCode } from '@nestjs/common'
import { HookParams } from './params.interface'
import { isEmpty } from 'lodash'
@Controller('deploymentsHook')
export class DeploymentsHookController {

  @Post('/v2/operator/deployment/hook/reconcile')
  @HttpCode(200)
  @UsePipes(new ValidationPipe({ transform: true }))
  public async reconcile(@Body() params: HookParams) : Promise<{status?: unknown, children: Record<string, unknown>[]}> {
    // console.log(JSON.stringify(params.children))
    const deploymentId = params.parent.spec.deploymentId
    // const specs = `select manifests from v2deployments where id = deploymentId`
    let specs = deploymentsSpec
    const deploymentNames = Object.keys(params.children['Deployment.apps/v1'])
    const allReady = deploymentNames.every(d => {
      const conditions = params.children['Deployment.apps/v1'][d].status.conditions
      return conditions.some(condition => condition.type === 'Available' && condition.status === 'True')
    })
    if (allReady) {
      specs = specs.concat(routesSpec)

    }
    return { children: specs }
  }

  @Post('/v2/operator/deployment/hook/finalize')
  @HttpCode(200)
  @UsePipes(new ValidationPipe({ transform: true }))
  public async finalize(@Body() params: HookParams): Promise<{ status?: unknown, children: Record<string, unknown>[], finalized: boolean, resyncAfterSeconds?: number }> {
    // console.log(JSON.stringify(params))
    const deploymentId = params.parent.spec.deploymentId
    let finalized = false
    let specs = deploymentsSpec
    console.log('=======================')
    console.log(JSON.stringify(params.children['Service.v1']))
    console.log('=======================')
    if (isEmpty(params.children['Service.v1'])) {
      specs = []
      // k8sClient.createRoutesResource(params.children)
      // - id: 123  << remove those
      //   components:
      //   - quiz-app-backend
      //   - quiz-app-frontend
    }
    if (isEmpty(params.children['Service.v1']) && isEmpty(params.children['Deployment.apps/v1'])) {
      // when everything is cleaned up update the database to signal the undeploy
      // deploymentRepository.update(params.parent.spec.deploymentId, {active: false})
      finalized = true
    }
    console.log(JSON.stringify(specs))
    if (finalized) {
      return { children: specs, finalized: finalized }
    }
    else {
      return { children: specs, finalized: finalized, resyncAfterSeconds: 2 }
    }
  }
}

// example spec to represent istio manifests
const routesSpec : Record<string, unknown>[] = [
  {
    'apiVersion': 'v1',
    'kind': 'Service',
    'metadata': {
      'labels': {
        'app': 'charlescd-ui',
        'service': 'charlescd-ui'
      },
      'name': 'charlescd-ui',
      'namespace': 'default'
    },
    'spec': {
      'ports': [
        {
          'name': 'http',
          'port': 3000,
          'targetPort': 3000
        }
      ],
      'selector': {
        'app': 'charlescd-ui'
      }
    }
  }
]

// example spec to represent helm manifests
const deploymentsSpec : Record<string, unknown>[]= [
  {
    'apiVersion': 'apps/v1',
    'kind': 'Deployment',
    'metadata': {
      'name': 'my-release-name',
      'namespace': 'default',
      'labels': {
        'app': 'charlescd-ui',
        'version': 'my-release-name',
        'component': 'front',
        'tag': 'v1',
        'circleId': 'circle-id-123'
      }
    },
    'spec': {
      'replicas': 1,
      'selector': {
        'matchLabels': {
          'app': 'charlescd-ui',
          'version': 'my-release-name',
          'component': 'front',
          'tag': 'v1',
          'circleId': 'circle-id-123'
        }
      },
      'template': {
        'metadata': {
          'annotations': {
            'sidecar.istio.io/inject': 'true'
          },
          'labels': {
            'app': 'charlescd-ui',
            'version': 'my-release-name',
            'component': 'front',
            'tag': 'v1',
            'circleId': 'circle-id-123'
          }
        },
        'spec': {
          'serviceAccountName': 'default',
          'containers': [
            {
              'name': 'darwin-ui-new-web',
              'image': 'hashicorp/http-echo',
              'args': ['-text', 'hello world'],
              'livenessProbe': {
                'failureThreshold': 3,
                'httpGet': {
                  'path': '/',
                  'port': 5678,
                  'scheme': 'HTTP'
                },
                'initialDelaySeconds': 20,
                'periodSeconds': 20,
                'successThreshold': 1,
                'timeoutSeconds': 1
              },
              'readinessProbe': {
                'failureThreshold': 3,
                'httpGet': {
                  'path': '/',
                  'port': 5678,
                  'scheme': 'HTTP'
                },
                'initialDelaySeconds': 20,
                'periodSeconds': 20,
                'successThreshold': 1,
                'timeoutSeconds': 1
              },
              'resources': {
                'limits': {
                  'cpu': '512m',
                  'memory': '512Mi'
                },
                'requests': {
                  'cpu': '128m',
                  'memory': '128Mi'
                }
              }
            }
          ]
        }
      }
    }
  }
]