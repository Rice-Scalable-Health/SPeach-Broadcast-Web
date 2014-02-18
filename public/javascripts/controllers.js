var sharedTextApp = angular.module('sharedTextApp', ["xeditable", 'textAngular', 'ngSanitize']);

// Boot strapping CSS for xeditables
sharedTextApp.run(function(editableOptions) {
    editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
});

sharedTextApp.factory('txt', function() {
    var str = "";
    var modify = {};
    modify.set = function(str_in) {
        str = str_in;
    }
    modify.append = function(item) {
        str = str + item + ' ';
        return 'added item';
    };
    modify.edit = function(before, after) {
        console.log("before: " + before + " after: " + after);
        console.log("replaced: " + str);
        str = str.replace(before, after);
        console.log("replaced: " + str);
    }
    modify.getString = function() {
        return str;
    };
    return modify;
});

// Object to keep track of all the utterances
sharedTextApp.factory('db', function(txt) {
    var str = "";
    var items = [];
    var modify = {};
    modify.addItem = function(index, item) {
        if (index > items.length - 1) {
            items.push({name: item});
            txt.append(item);
            console.log(item);
        }
        else if (items[index].name != item) {
            txt.edit(items[index].name, item);
            items[index].name = item;
            console.log("modified");
        }
        return 'added item';
    };
    modify.getItems = function() {
        return items;
    };
    modify.getString = function() {
        str = "";
        for (var s in items)
            str = str.concat(items[s].name, ' ');
        return str;
    }
    return modify;
});

// Controller
sharedTextApp.controller('SharedTxtCtrl', function($scope, $http, $filter, db, txt) {
	
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

            // update textAngular
            $scope.htmlcontent = txt.getString();
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

        // update textAngular
        $scope.htmlcontent = txt.getString();

    };

    $scope.save = function(){
        txt.set($scope.htmlcontent);
    };

    // Array holding all the utterances
    $scope.editables = db.getItems();


    // The rest is needed for select. This is here just for testing
    $scope.user = {
        status: 2
    }; 

    $scope.statuses = [
        {value: 1, text: 'status1'},
        {value: 2, text: 'status2'},
        {value: 3, text: 'status3'},
        {value: 4, text: 'status4'}
    ]; 

    $scope.showStatus = function() {
        var selected = $filter('filter')($scope.statuses, {value: $scope.user.status});
        return ($scope.user.status && selected.length) ? selected[0].text : 'Not set';
    };
});
