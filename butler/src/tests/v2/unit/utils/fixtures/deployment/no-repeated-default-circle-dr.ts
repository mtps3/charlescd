export const noRepeatedDefaultCircleDr = {
  apiVersion: 'networking.istio.io/v1alpha3',
  kind: 'DestinationRule',
  metadata: {
    name: 'A',
    namespace: 'sandbox'
  },
  spec: {
    host: 'A',
    subsets: [
      {
        labels: {
          circleId: 'default-circle-id',
          component: 'A',
          tag: 'v1'
        },
        name: 'default-circle-id'
      },
      {
        labels: {
          circleId: 'normal-circle-id',
          component: 'A',
          tag: 'v1'
        },
        name: 'normal-circle-id'
      }
    ]
  }
}