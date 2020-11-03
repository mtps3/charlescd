/*
 *
 *  Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package gitlab

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
)

type Credentials struct {
	Token string `json:"token"`
}

type RepoData struct {
	RepoID  string `json:"repoId"`
	RepoURL string `json:"repoUrl"`
}

func (client APIClient) DownloadRepo(repoData, credentials json.RawMessage) ([]byte, error) {
	var rd RepoData
	err := json.Unmarshal(repoData, &rd)
	if err != nil {
		return nil, err
	}

	var cred Credentials
	if credentials != nil {
		err = json.Unmarshal(credentials, &cred)
		if err != nil {
			return nil, err
		}
	}

	req, err := http.NewRequest("GET", fmt.Sprintf("%s/api/v4/projects/%s/repository/archive", rd.RepoURL, rd.RepoID), nil)
	if err != nil {
		return nil, err
	}

	if cred != (Credentials{}) {
		req.Header.Add("PRIVATE-TOKEN", cred.Token)
	}

	resp, err := client.httpClient.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	return ioutil.ReadAll(resp.Body)
}