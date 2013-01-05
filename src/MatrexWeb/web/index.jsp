<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd"> 
 
<html lang="en"> 
<head> 
    <title>MaTrEx</title> 
    <meta charset="UTF-8" />
    <meta name="viewport" content="user-scalable=no, width=device-width" />
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <link rel="apple-touch-icon" href="images/icon.png" />  <!-- Home screen icon -->
    <link rel="apple-touch-startup-image" href="images/loading.png" />  <!-- Loading screen image -->
    <link rel="stylesheet" type="text/css" href="mobile.css" media="screen" />
    <script type="text/javascript" src="jquery.js"></script> 
    <script type="text/javascript" src="mobile.js"></script>
</head> 
<body onload="calcTextBox()" onresize="calcTextBox()">
    <div id="container" align="center">
        <div id="header"> 
            <h1>MaTrEx</h1>
        </div> 
 
        <div id="contents"> 
            <div id="settings" class="showset">
                <div id="done_but" class="but" onclick="toggleSettings()">Done</div>
                Choose subject:
                <select id="subject"></select><br />
                Choose search engine:
                <select id="engine"></select>
            </div> 
 
            <div id="application">
            	 <!-- settings (subject of input, font size etc.) -->
            	<div id="holder">
                    <div id="ref_but" class="but" onclick="refresh()"><img src="images/refresh.png" /></div>
                    <div id="set_but" class="but" onclick="toggleSettings()">Settings</div>
                </div>
                <div id="trans_but" class="but" onclick="translateText()">Translate</div>
 
                <!-- input language selections --> 
                <div class="choice">
                    <div id="language1">
                        From:<br />
                        <select id="in" onchange="selectImage()"></select>
                    </div>

                    <img class="pic" id="inpic"/>
                </div>

                <!-- input text for translation -->
                <textarea id="text" onclick="document.execCommand('selectAll',false,null)"></textarea><br />
                                        <!-- Selects text in input box when pressed, useful for user -->

                <!-- output language selections -->
                <div class="choice">
                    <div id="language2">
                        To:<br />
                        <select id="out" onchange="selectImage()"></select>
                    </div>

                    <img class="pic" id="outpic"/>
                </div>

                <!-- output text from translation -->
                <textarea id="output" onkeyup="getChange()" disabled="disabled"></textarea>

                <!-- Decision buttons - what user decides to do with output -->
                <div id="decbuts">
                    <button id="edbutton" onclick="allowEdits()">Edit output</button>
                    <button id="pebutton" class="showbut" onclick="submitEdits()">Submit</button>
                    <br />
                    <button onclick="makeMailto()">Email</button><br />
                    <button onclick="makeSearch()">Search web</button>
                </div>
            </div> 
        </div> 
    </div> 
</body> 
</html>
