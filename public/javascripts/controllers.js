var sharedTextApp = angular.module('sharedTextApp', ["xeditable"]);

// Boot strapping CSS for xeditables
sharedTextApp.run(function(editableOptions) {
    editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
});

// Object to keep track of all the utterances
sharedTextApp.factory('db', function() {
    var items = [];
    var modify = {};
    modify.addItem = function(index, utteranceIndex, optionIndex, item, blocked) {
        if (index > items.length - 1)
            items.push({name: item, utteranceIndex: utteranceIndex, optionIndex: optionIndex, blocked: blocked});
        else if (items[index].name != item || items[index].blocked != blocked) {
            items[index].name = item;
            items[index].utteranceIndex = utteranceIndex;
            items[index].optionIndex = optionIndex;
            items[index].blocked = blocked;
        }
        return 'added item';
    };
    modify.getItems = function() {
        return items;
    };
    return modify;
});

// Controller
sharedTextApp.controller('SharedTxtCtrl', function($scope, $http, $filter, db) {
	
	// Event Listeners
    var source = new EventSource('api/stream');

    // Update from Server's event
	source.addEventListener('message', function(e) {
	    $scope.$apply(function() {
            var index = 0;

            var dataJSON = JSON.parse(e.data);

            for (var utteranceId in dataJSON) {
                var utterance = dataJSON[utteranceId];
                // add in this code for when we have options.
                for (var optionId in utterance) {
                    var option = utterance[optionId];
                    // add item to db with utteranceId and optionId
                    db.addItem(index++, utteranceId, optionId, option.text, option.blocked);
                }
            }
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

    $scope.blockOption = function(index) {
        var editable = $scope.editables[index];

        if (!editable.blocked) {
            $http({
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                url: 'api/blockoption',
                data: [editable.utteranceIndex, editable.optionIndex]
            });
        }
    };

    $scope.unblockOption = function(index) {
        var editable = $scope.editables[index];

        $http({
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            url: 'api/unblockoption',
            data: [editable.utteranceIndex, editable.optionIndex]
        });

        $scope.openXEditableIndex = -1;
    };

    // Function for the xeditables to send data to server
    // Requires the index of text to edit and the updated text
    $scope.sendDataFromEditables = function(index, text) {
        var editable = $scope.editables[index];
        $http({
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            url: 'api/modify',
            data: [editable.utteranceIndex, editable.optionIndex, text]
        });
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

    $scope.saveXEditableIndex = function(index) {
        $scope.openXEditableIndex = index;
    }

    window.onbeforeunload = function() {
        if ($scope.openXEditableIndex) {
            $scope.unblockOption($scope.openXEditableIndex);
        }

        return "Make sure you save/cancel changes before you leave the page";
    };
});
