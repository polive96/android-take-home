/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function () {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function () {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function () {
        app.receivedEvent('deviceready');
    },
    // Update DOM on a Received Event
    receivedEvent: function (id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    }
};




//NATIVE FUNCTIONS
//----------------------------

//shows a toast message in the android OS
var showNativeAlert = function(msg) {
    //send an alert message to the cordova communication plugin
    cordova.exec(function(success) {}, //success callback
             function(error) {}, //error callback
             "CommunicationPlugin", //class name
             "alert", //action name
             [msg] //arguments
    );
};

var minimizeChatHead = function() {
    //send an minimize message to the cordova communication plugin
        cordova.exec(function(success) {}, //success callback
                 function(error) {}, //error callback
                 "CommunicationPlugin", //class name
                 "minimize", //action name
                 [] //arguments
        );
};

var closeChatHead = function() {
    //send an close message to the cordova communication plugin
        cordova.exec(function(success) {}, //success callback
                 function(error) {}, //error callback
                 "CommunicationPlugin", //class name
                 "close", //action name
                 [] //arguments
        );
};

//----------------------------






//NATIVE JS FUNCTIONS
//----------------------------

var nativeToJsApi = {};
//shows a javascript based toast message in the webview
nativeToJsApi.actionShowAlert = function(msg) {
    M.toast({html: msg, classes: 'rounded'});
};

nativeToJsApi.actionChangeBackground = function() {
    let colorArray = [];
    for(let i =0; i < 3 ; i++){
        colorArray.push(Math.floor(Math.random() * (255 - 0) + 0));
    }
    // rgb -> hex
    let color = colorArray
    .map( x => x.toString(16))
    .join('');
    document.body.style.background = "#" + color;
};

//----------------------------




//HTML ELEMENT SETUP
//----------------------------

//add click functionality to the buttons on the screen
document.addEventListener('DOMContentLoaded', function () {
  document.querySelector('#alert_button').addEventListener('click',
    function clickHandler(element) {
       document.activeElement.blur();
       showNativeAlert('This is a message from the Webview to Android!');
    });
  document.querySelector('#minimize_button').addEventListener('click',
      function clickHandler(element) {
         document.activeElement.blur();
         minimizeChatHead();
      });
  document.querySelector('#close_button').addEventListener('click',
      function clickHandler(element) {
         document.activeElement.blur();
         closeChatHead();
      });
});

//----------------------------





//initialize cordova
app.initialize();
