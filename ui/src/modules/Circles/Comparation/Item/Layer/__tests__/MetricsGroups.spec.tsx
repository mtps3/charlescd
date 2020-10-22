/*
 * Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React from 'react';
import { render, screen } from 'unit-test/testUtils';
import MetricsGroups from '../MetricsGroups';
import userEvent from '@testing-library/user-event';

test('render Metrics Groups Layer without data', async () => {
  const handleClick = jest.fn();

  render(<MetricsGroups circleId={'1'} onClickCreate={handleClick}/>);

  const addMetricsGroups = await screen.findByTestId('button-iconRounded-add');

  expect(screen.getByTestId('layer-metrics-groups')).toBeInTheDocument();
  expect(screen.getByText('Metrics Groups')).toBeInTheDocument();
  expect(addMetricsGroups).toBeInTheDocument();

  userEvent.click(addMetricsGroups);
  expect(handleClick).toHaveBeenCalled();
});
