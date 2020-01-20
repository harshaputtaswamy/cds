/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2019 Orange. All rights reserved.
===================================================================

Unless otherwise specified, all software contained herein is licensed
under the Apache License, Version 2.0 (the License);
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
============LICENSE_END============================================
*/

import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ApiService} from '../../../../common/core/services/api.typed.service';
import {ResourceDictionaryURLs} from '../../../../common/constants/app-constants';
import {ModelType} from '../model/ModelType.model';


@Injectable({
    providedIn: 'root'
})
export class DesignerService {

    constructor(private api: ApiService<ModelType>) {
    }

    getFunctions(modelDefinitionType: string): Observable<ModelType[]> {
        return this.api.get(ResourceDictionaryURLs.getResourceDictionary + '/' + modelDefinitionType);
    }

}
