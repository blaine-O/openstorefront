/* 
 * Copyright 2014 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


var doAttributes = function(contentId, callback) {
    if (contentId === undefined || contentId === null){
      contentId = '#content';
    }
    $.get("/openstorefront/api/v1/resource/attributes", function(data) {
        if (data && data.length > 0){
			data.sort();
            if (data[0].codes !== undefined) {
              setupAttributes(data, contentId);
            } else {
              $(contentId).append('(requires login to view)');
            }
        }
        callback();
    });
};


var setupAttributes = function(types, contentId) {
	types.sort(function(a, b){
		return a.attributeType.localeCompare(b.attributeType);
	});
    for (var i = 0; i < types.length; i++) {
        var codes = types[i].codes;
        if (codes && codes.length > 0) {
            $('#tableOfContents').append('<tr><td><span goTo="' + types[i].attributeType + '" class="imitateLink">' + types[i].description + '</span></td></tr>');
            $(contentId).append('<div id="' + types[i].attributeType + '" style="margin-top: 50px;"><h3>' + types[i].description + ' (' + types[i].attributeType + ')</h3><div style="margin-left: 20px;"><table><tr><th>Code</th><th>Label</th><th>Description</th></tr></table></div></div>');
            for (var j = 0; codes && j < codes.length; j++) {
                $('#' + types[i].attributeType).find('table').append('<tr><td>' + codes[j].code + '</td><td>' + codes[j].label + '</td><td>' + codes[j].description + '</td></tr></div>');
            }
        }
    }
    $('span[goTo]').on('click', function(e) {
        e.preventDefault();
        var target = $(this).attr('goTo');
        var $target = $('#' + target);

        $('html, body').stop().animate({
            'scrollTop': $target.offset().top - 50
        }, 400, 'swing', function() {
        });
    });

};

var doLookups = function(contentId) {
    if (contentId === undefined || contentId === null){
      contentId = '#content';
    }
    $.get("/openstorefront/api/v1/resource/lookuptypes", function(data) {
        if (data && data.length > 0) {
            //console.log('data', data);
          if (data[0].code !== undefined) {
            var types = data;
			types.sort(function(a, b){
				return a.code.localeCompare(b.code);
			});			
            for (var i = 0; i < types.length; i++) {
              setupLookups(types[i], contentId);
            }
          } else {            
            $(contentId).append('(requires login to view)');            
          }
        }
    });
};


var setupLookups = function(type, contentId) {
    $.get("/openstorefront/api/v1/resource/lookuptypes/" + type.code, function(codes) {
        if (codes && codes.length > 0) {
            $('#tableOfContents').append('<tr><td><span goTo="' + type.code + '" class="imitateLink">' + type.code + '</span></td></tr>');
            $(contentId).append('<div id="' + type.code + '" style="margin-top: 50px;"><h3>' + type.code + '</h3><div style="margin-left: 20px;"><table><tr><th>Code</th><th>Description</th></tr></table></div></div>');
            $('span[goTo]').on('click', function(e) {
                e.preventDefault();
                var target = $(this).attr('goTo');
                var $target = $('#' + target);

                $('html, body').stop().animate({
                    'scrollTop': $target.offset().top - 50
                }, 400, 'swing', function() {
                });
            });
            for (var j = 0; codes && j < codes.length; j++) {
                $('#' + type.code).find('table').append('<tr><td>' + codes[j].code + '</td><td>' + codes[j].description + '</td></tr></div>');
            }
        }

    });
};
