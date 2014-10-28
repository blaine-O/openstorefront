/* 
* Copyright 2014 Space Dynamics Laboratory - Utah State University Research Foundation.
*
* Licensed under the Apache License, Version 2.0 (the 'License');
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an 'AS IS' BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
'use strict';

app.factory('configurationservice', ['localCache', '$http', '$q', function(localCache, $http, $q) { /*jshint unused: false*/

  var service = {};
  var minute = 60 * 1000;

  /***************************************************************
  * This function is used to check the localCache for the existance of a result
  * object that hasn't yet expired
  * params: name -- The unique identifier for the entry in the local cache (usually a string)
  * params: expire -- The ammount of time in ms that it will take for the object to expire
  * returns: result -- The value of the object if it has not yet expired, and null for
  *                    result objects that are no longer valid
  ***************************************************************/
  var checkExpire = function(name, expire) {
    var result = localCache.get(name, 'object');
    var cacheTime = null;
    if (result) {
      cacheTime = localCache.get(name+'-time', 'date');
      var timeDiff = new Date() - cacheTime;
      if (timeDiff < expire) {
        return result;
      } else {
        return null;
      }
    }
    return null;
  };

  /***************************************************************
  * We use this function in conjunction with the checkExpire function.
  * Use this function to save the value in the local cache (it will also save
  * an expire time that it can use later to check validity of an entry)
  * params: name -- The unique identifier for the entry in the local cache (usually a string)
  * params: value -- The value of the data that you will be storing
  ***************************************************************/
  var save = function(name, value) {
    localCache.save(name, value);
    localCache.save(name+'-time', new Date());
  };

  var updateCache = function(name, value) {
    save(name, value);
  };


  service.getXRefTypes = function() {
    var deferred = $q.defer();
    deferred.resolve([
    {
      'attributeType': 'DI2ELEVEL',
      'fieldName': 'Custom_field-16002',
      'key': 'value',
      'mappingName': 'Test',
      'projectType': 'Asset',
      'issueType': 'Asset',
      'integrationType': 'jira'
    }
    ]);
    return deferred.promise;
  }

  service.getConfigId = function(mapping) {
    var deferred = $q.defer();


    // if the mapping has the same attribute as a previously made config, send back that config
    // grab the configuration by id!
    if (mapping.attributeType === 'DI2ELEVEL') {
      deferred.resolve({
        'id': 2304923,
        'type': 'jira',
        'issueNumber': 'ASSET-151',
        'componentId': '67', 
        'overRideRefreshRate': '0 4 4 * * *',
        'status': 'error',
        'errorMessage': 'The ticket was not found' 
      });
    } else {
      deferred.resolve(false);
    }


    return deferred.promise;
  }

  service.getConfigurations = function(mapping) {

    var deferred = $q.defer();


    // if the mapping has the same attribute as a previously made config, send back that config
    if (mapping.attributeType === 'DI2ELEVEL') {
      deferred.resolve([{
        'id': 2304923,
        'type': 'jira',
        'issueNumber': 'ASSET-151',
        'componentId': '67', 
        'overRideRefreshRate': '0 4 4 * * *',
        'status': 'error',
        'errorMessage': 'The ticket was not found' 
      }]);
    } else {
      deferred.reject(false);
    }


    return deferred.promise;
  }

  service.getProjects = function() {
    var deferred = $q.defer();
    var value = null;/*checkExpire('JiraProjects', minute * 1440);*/
    if (value) {
      deferred.resolve(value);
    } else {
      var url = 'api/v1/service/jira/projects'
      $http({
        'method': 'GET',
        'url': url
      }).success(function(data, status, headers, config){
        if (data && isNotRequestError(data)) {
          save('JiraProjects', data);
          deferred.resolve(data);
        } else {
          deferred.reject(false);
        }
      }).error(function(){
        deferred.reject(false);
      });
    }

    return deferred.promise;
  }

  service.getIssueOptions = function(project) {
    var deferred = $q.defer();
    if (project && project.code) {
      var value = null;/*checkExpire(project.code+'-IssueOptions', minute * 1440);*/
      if (value) {
        deferred.resolve(value);
      } else {
        var url = 'api/v1/service/jira/projects/'+project.code;
        $http({
          'method': 'GET',
          'url': url
        }).success(function(data, status, headers, config){
          if (data && isNotRequestError(data)) {
            save(project.code+'-IssueOptions', data);
            deferred.resolve(data);
          } else {
            deferred.reject(false);
          }
        }).error(function(){
          deferred.reject(false);
        });
      }

    } else {
      deferred.reject(false);
    }
    return deferred.promise;
  }

  service.getJiraFields = function(project, issueType) {
    var deferred = $q.defer();
    if (project && project.code && issueType && issueType.name) {
      var value = null;/*checkExpire(project.code+'-'+issueType.name+'-fields', minute * 1440);*/
      if (value) {
        deferred.resolve(value);
      } else {
        var url = 'api/v1/service/jira/projects/'+project.code+'/'+issueType.name+'/fields';
        $http({
          'method': 'GET',
          'url': url
        }).success(function(data, status, headers, config){
          if (data && isNotRequestError(data)) {
            save(project.code+'-'+issueType.name+'-fields', data);
            deferred.resolve(data);
          } else {
            deferred.reject(false);
          }
        }).error(function(){
          deferred.reject(false);
        });
      }

    } else {
      deferred.reject(false);
    }
    return deferred.promise;
  }

  service.getStoreFields = function() {
    var deferred = $q.defer();
    var value = null;/*checkExpire('attributeTypes', minute * 1440);*/
    if (value) {
      deferred.resolve(value);
    } else {
      var url = 'api/v1/resource/attributes/attributetypes';
      $http({
        'method': 'GET',
        'url': url
      }).success(function(data, status, headers, config){
        if (data && isNotRequestError(data)) {
          save('attributeTypes', data);
          deferred.resolve(data);
        } else {
          deferred.reject(false);
        }
      }).error(function(){
        deferred.reject(false);
      });
    }
    return deferred.promise;
  }

  service.getStoreCodes = function(attribute) {
    var deferred = $q.defer();
    if (attribute && attribute.attributeType) {

      var value = null;/*checkExpire('attributeCodes-'+attribute.attributeType, minute * 1440);*/
      if (value) {
        deferred.resolve(value);
      } else {
        var url = 'api/v1/resource/attributes/attributetypes/'+attribute.attributeType+'/attributecodes';
        $http({
          'method': 'GET',
          'url': url
        }).success(function(data, status, headers, config){
          if (data && isNotRequestError(data)) {
            save('attributeCodes-'+attribute.attributeType, data);
            deferred.resolve(data);
          } else {
            deferred.reject(false);
          }
        }).error(function(){
          deferred.reject(false);
        });
      }
    } else {
      deferred.reject(false);
    }
    return deferred.promise;
  }

  service.saveMappingConf = function(mapping) {
    var deferred = $q.defer();
    var url = 'api/v1/resource/integration/mapping';
    $http({
      'method': 'POST',
      'url': url,
      'data': mapping
    }).success(function(data, status, headers, config){
      if (data && isNotRequestError(data)) {
        deferred.resolve(data);
      } else {
        deferred.reject(false);
      }
    }).error(function(data, status, headers, config){
      if (status !== 201) {
        deferred.reject(false);
      } else {
        deferred.resolve(data);
      }
    });
    return deferred.promise;
  }

  service.getMappingTypes = function() {
    var deferred = $q.defer();
    var url = 'api/v1/resource/integration/mapping/types';
    var value = null; /*checkExpire('previousMappings', minute * 1440);*/
    if (value) {
      deferred.resolve(value);
    } else {
      $http({
        'method': 'GET',
        'url': url,
      }).success(function(data, status, headers, config){
        if (data && isNotRequestError(data)) {
          console.log('data', data);
          deferred.resolve(data);
        } else {
          deferred.reject(data);
        }
      }).error(function(data, status, headers, config){
        deferred.reject(false);
      });
    }
    return deferred.promise;
  }


  return service;

}]);