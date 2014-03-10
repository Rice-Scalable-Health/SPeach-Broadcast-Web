var sharedTextApp = angular.module('sharedTextApp', ['textAngular', 'xeditable']);

// Boot strapping CSS for xeditables
sharedTextApp.run(function(editableOptions) {
    editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
});

// Object to keep track of all the utterances
sharedTextApp.factory('db', function() {
    var html = "";
    var str = "";
    var markup = "";

    var items = [];
    var modify = {};
    // 
    modify.addItem = function(index, item) {
        if (index > items.length - 1) {
            items.push({name: item, isHTML: true});
        }
        else if (items[index].name != item && items[index].isHTML) {
            items[index].name = item;
        }
        return 'added item';
    };
    //
    modify.getItems = function() {
        return items;
    };
    //
    modify.storeMarkup = function(newVal) {
        // newVal = newVal.replace("</div>", "");
        // newVal = newVal.replace("</span><br></li>", "");
        // newVal = newVal.replace("</ul>", "");
        // newVal = newVal.replace("</li>", "");
        markup = newVal;
        return "stored";
    };
    //
    modify.addMarkup = function(len) {
        for (var s = 0; s < len;) {
            if (markup.search(items[s].name) >= 0) {
                if (markup.search(items[s].name.concat(' ')) >= 0) {
                    markup = markup.replace(items[s].name.concat(' '), "");
                } else {
                    markup = markup.replace(items[s].name, "");
                }
                
                s++;
            } else {
                // find the next match
                var n = 0;
                var i = s;
                for (; i < len; i++) {
                    if (markup.search(items[i].name) >= 0) {
                        n = markup.indexOf(items[i].name);
                        break;
                    }
                }

                var res = "";
                if (n == 0)
                    res = markup;
                else
                    res = markup.substring(0,n);

                // Process the resulting string
                markup = markup.replace(res, "");

                items[s].name = res;
                items[s].isHTML = false;

                // deal with all others that was skipped
                for (var j = s+1; j < i; j++){
                    items[j].name = "";
                    items[j].isHTML = false;
                }

                s = i;
            }
        }

        // If there is something left at the end, append it to the very last index
        if (0 != markup.length) {
            items[len-1].name = items[len-1].name.concat(markup);
            items[len-1].isHTML = false; // Every last one cannot be marked up
        }

        return 'marked up';
    };
    //
    modify.getHTML = function() {
        html = "";
        for (var s in items) {
            if (items[s].isHTML)
                html = html.concat("<a href=\"#\" editable-text=\"editables[", s.toString(), "].name\" onbeforesave=\"sendDataFromEditables(", s.toString(), ", $data)\">{{ editables[", s.toString(), "].name || \"empty\" }} </a>");
            else
                html = html.concat(items[s].name);
        }
        return html;
    }
    //
    modify.getString = function() {
        str = "";
        for (var s in items) {
            str = str.concat(items[s].name, ' ');
        }
        return str;
    }
    //
    modify.getLen = function() {
        return items.length;
    }
    return modify;
});

// Controller for the htmlcontent
sharedTextApp.controller('test', function($scope, db){
    $scope.$watch('htmlcontent', function(newVal){
        console.log(newVal);
        db.storeMarkup(newVal);
    });
});

// Controller
sharedTextApp.controller('SharedTxtCtrl', function($scope, $http, $filter, $sce, db) {
	
	// Event Listeners
    var source = new EventSource('api/stream');

    // Update from Server's event
	source.addEventListener('message', function(e) {
	    $scope.$apply(function() {
            var index = 0;

            var dataJSON = JSON.parse(e.data);

            for (var utteranceId in dataJSON) {
                var utterance = dataJSON[utteranceId];
                db.addItem(index++, utterance.text);
                // add in this code for when we have options.
//                for (var optionId in utterance) {
//                    var option = utterance[optionId];
//                    db.addItem(index++, option.text);
//                }
            }

            // update directive template
            $scope.html = db.getHTML();

	    });
	}, false);

	source.addEventListener('open', function(e) {
	}, false);

	source.addEventListener('error', function(e) {
	}, false);

	// Function for the input box to send data to server
    $scope.sendData = function() {
        $http({
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain',
            },
            url: 'api/add',
            data: $scope.inputText
        });

        $scope.inputText = ""
    };

    // Function for the xeditables to send data to server
    // Requires the index of text to edit and the updated text
    $scope.sendDataFromEditables = function(index, text) {
        $http({
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            url: 'api/modify',
            data: [index, text]
        });

        // update local
        db.addItem(index, text);

    };

    // Change view
    $scope.selection = 'view';

    $scope.getView = function() {
        if ($scope.selection != 'view') {
            $scope.selection = 'view';
            db.addMarkup($scope.lastIndex);
            $scope.html = db.getHTML();
        }
    }

    $scope.getMarkup = function() {
        if ($scope.selection != 'markup'){
            $scope.selection = 'markup';
            $scope.htmlcontent = db.getString();
            $scope.lastIndex = db.getLen();
        }
    }

    // Initialization of variables
    $scope.editables = db.getItems();
    $scope.html = db.getHTML();
    $scope.htmlcontent = "";

});

// Directive
sharedTextApp.directive('helloWorld', function ($compile, db) {
    var linker = function(scope, element, attrs) {
        reload = function() {
            template = angular.element(element.html(scope.html));
            $compile(template.contents())(scope);
        }

        scope.$watch('html', function() {reload();});
    }

    return {
        restrict: 'E',
        replace: true,
        link: linker,
        //template: '<a href="#" editable-text="editables[1].name" onbeforesave="sendDataFromEditables(1, $data)">{{ editables[1].name || "empty" }}</a>',
        //template: '<p ng-bind-html="output"></p>',
    };
});
