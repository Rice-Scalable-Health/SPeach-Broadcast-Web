// var sessionId is passed in from the view.

var sharedTextApp = angular.module('sharedTextApp', ["monospaced.elastic"]);

sharedTextApp.controller('SharedTxtViewCtrl', function($scope, $http, $location, $anchorScroll) {
    $scope.utterances = [];

    // Event Listeners
    var source = new EventSource('/api/transcript/'+sessionId);

    // Update from Server's event
    source.addEventListener('message', function(e) {
        $scope.$apply(function() {
            $scope.utterances = JSON.parse(e.data);

            if ($scope.autoscroll) {
                $location.hash('bottom');
                $anchorScroll();
            }
        });
    }, false);

    $scope.requestHelp = function(index) {
        $http({
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain',
            },
            url: '/api/requesthelp/'+sessionId,
            data: index
        });
    };


    source.addEventListener('open', function(e) {
    }, false);

    source.addEventListener('error', function(e) {
    }, false);

    $scope.transcript = "";
});

// Object to keep track of all the utterances
sharedTextApp.factory('db', function() {
    var str = "";
    var modify = {};

    modify.append = function(newVal) {
        str = str.concat(newVal);
        return "appended";
    };

    modify.store = function(newVal) {
        str = newVal;
        return "stored";
    };

    modify.getString = function() {
        return str;
    }

    return modify;
});

// Controller
sharedTextApp.controller('SharedTxtCtrl', function($scope, $http, $timeout, db) {
    // boolean that stops the initial modification http request from occuring.
    var isLoading = true;
    
    // Event Listeners
    var source = new EventSource('/api/stream/'+sessionId);

    // Update from Server's event
    source.addEventListener('message', function(e) {
        var caretPos = getCaret('volunteer_textarea');

        $scope.$apply(function() {
            // hacky solution for SSE not sending newlines: use tabs instead, so we need to replace tabs here.
            var transcriptWithNewLines = e.data.replace(/\t/g, "\n");

            var splitToAdd = transcriptWithNewLines.split("###");
            transcriptWithNewLines = splitToAdd[0];

            // if there is a request for help.
            if (splitToAdd.length > 1) {
                var indexToHelp = splitToAdd[1];

                var newTranscript = "";

                var fullTranscript = db.getString();
                var fullTranscriptSplit = fullTranscript.split("\n");
                fullTranscriptSplit[indexToHelp] = "**"+fullTranscriptSplit[indexToHelp];

                var prefix = "";
                for (var i = 0; i < fullTranscriptSplit.length; i++) {
                    newTranscript += prefix;
                    newTranscript += fullTranscriptSplit[i];
                    prefix = "\n";
                }

                db.store(newTranscript);
            }


            if (transcriptWithNewLines !== db.getString()) {
                db.append(transcriptWithNewLines);
            }

            $scope.htmlcontent = db.getString();
        });
        setCaretPosition('volunteer_textarea', caretPos);
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
            url: '/api/add/'+sessionId,
            data: $scope.inputText
        });

        $scope.inputText = ""
    };

    // Function for the textAngular to send data to server
    $scope.sendData2 = function(text) {
        $http({
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain',
            },
            url: '/api/modify/'+sessionId,
            data: text
        });
    };

    $scope.$watch('htmlcontent', function(newVal){
        if (!isLoading) {
            console.log(newVal);
            db.store(newVal);
            $scope.htmlcontent = db.getString();
            $scope.sendData2(newVal);
        }
        isLoading = false;
    });

    // Initialization of variables
    $scope.htmlcontent = "";

    function setCaretPosition(elemId, caretPos) {
        var elem = document.getElementById(elemId);

        if(elem != null) {
            if(elem.createTextRange) {
                var range = elem.createTextRange();
                range.move('character', caretPos);
                range.select();
            }
            else {
                if(elem.selectionStart) {
                    elem.focus();
                    elem.setSelectionRange(caretPos, caretPos);
                }
                else
                    elem.focus();
            }
        }
    }

    function getCaret(elemId) {
        var el = document.getElementById(elemId);

        if (el.selectionStart) { 
            return el.selectionStart; 
        }
        else if (document.selection) { 
            el.focus(); 

            var r = document.selection.createRange(); 
            if (r == null) { 
                return 0; 
            } 

            var re = el.createTextRange(), 
            rc = re.duplicate(); 
            re.moveToBookmark(r.getBookmark()); 
            rc.setEndPoint('EndToStart', re); 

            return rc.text.length; 
        }  
        return 0; 
    }

});
