//Global variables
var startTime;
var transid;
var changed = false;
var editing = false;
var searchInfo = new Array();

$(document).ready(function(){
    var sources = new Array(); ar targets = new Array(); var domains = new Array(); 
    var i = 0;var j = 0;
    //Get language pair and search engine info from xml file and insert as options
    $.ajax({
        type: "GET",
        url: "settings.xml",
        dataType: "xml",
        success: function(xml) {
			//For each language pair
            $(xml).find('pair').each(function(){
                var sourceVal = $(this).find('source').text();
                sources[i] = "<option value=\"" + sourceVal + "\">" + sourceVal + "</option>";

                var targetVal = $(this).find('target').text();
                targets[i] = "<option value=\"" + targetVal + "\">" + targetVal + "</option>";

                var domainVal = $(this).find('domain').text();
                domains[i] = "<option value=\"" + domainVal + "\">" + domainVal + "</option>";

                i++;
            });
			
            //There will be repeats added to the language pair arrays. Get uniques.
            sources = getUniques(sources);targets = getUniques(targets);domains = getUniques(domains);

            //Add everything from each array as options
            for(j=0; j<sources.length; j++) {
                $('#in').append(sources[j]);
            }
            for(j=0; j<targets.length; j++) {
                $('#out').append(targets[j]);
            }
            for(j=0; j<domains.length; j++) {
                $('#subject').append(domains[j]);
            }

            //Once option is loaded select the image
            selectImage();

			//For each search engine
            $(xml).find('engine').each(function(){
                var engineVal = $(this).find('name').text();
                searchInfo[j] = engineVal; j++;	//Enters name of search engine
                $('#engine').append("<option value=\"" + engineVal + "\">" + engineVal + "</option>");
                searchInfo[j] = $(this).find('searchurl').text(); j++;	//After the engine name is the parameterless engine URI
            });
        }
    });
});

function selectImage() {
    //language text is name of flag
    var inLink = "images/flags/" + $('#in').val() + ".gif";
    $('#inpic').attr('src', inLink);

    var outLink = "images/flags/" + $('#out').val() + ".gif";
    $('#outpic').attr('src', outLink);
}

//For varying screen sizes and orientation changes.
function calcTextBox() {
    var winHeight = $(window).height();

    var headerHeight = $('#header').height();
    //choice section height is greater of the two elements that make it up
    var choiceHeight = $('#language1').height() > $('#outpic').height() ? $('#language1').height() : $('#outpic').height();
    var decbutsHeight = $('#decbuts').height();

    //Divide the remaining height (if there is any) among the two textareas
    var boxHeight = (winHeight - (headerHeight + 2*choiceHeight + decbutsHeight))/2 - winHeight/10;
    if(boxHeight <= 20) {
        boxHeight = 20;
    }

    $("textarea").css({"height":boxHeight});
    $('#outputdiv').css({"height":boxHeight});
}


////For post-editing
//Displays submit button once changes have been made to translation
function toggleButton() {
    $('#pebutton').toggleClass('showbut');
    var d = new Date();
    startTime = d.getTime();
}

//Toggles settings and the app so page doesn't reload.
//Also toggles button value
var index = 0;
function toggleSettings() {
    $('#settings').toggleClass('showset');
    $('#application').toggleClass('hideapp');
}

var editid;
function translateText() {
    var inputStr = $('#text').val().replace(/(\r\n|\n|\r)/gm," ");  //Replace return characters
	
    if(inputStr.length > 0) {   //If there is input
        editid = 0; changed = false; //User has started translation process, set global variables
        if(editing) {  //If user decides to translate again without refreshing rehide the submit button
            $('#pebutton').toggleClass('showbut');     //hide submit button for editing (not using method, not resetting time)
            $("#output").attr("disabled","disabled");   //Disable text area
            editing = false;
        }

        $('body').append('<div id="progress">Loading...</div>');    //Showing loading div
                                        //Encoding uri to look after special characters
        $.get('MatrexServlet', {text: encodeURI(inputStr), inl: $('#in').val(), outl: $('#out').val(), subject: $('#subject').val()}, function(responseText) {
            var res = $(responseText);
            //Response is returned in xml form with the translation and traslation id
            $('#output').text(res.find('translation').text());
            transid = res.find('transid').text();   //Each translation gets new id from server
            $('#progress').remove();    //remove progress div
        })
        .error(function() { //alert of error
            $('#progress').remove();
            alert("Sorry, there was an error. Try again later.");
        });
    }
    else {
        alert("Please enter text to translate.")
    }
}

//Calls trackeditsservlet with the text in the output box
function getChange() {
    changed = true;
    $.get('TrackEditsServlet', {editid: editid, transid: transid, text: encodeURI($('#output').val())}, function() {
        //Each time it's called a new editid is passed for the transid in question
        editid++;
    })
}

//decide whether to allow editing
function allowEdits() {
   if(!editing) {   //If the user is not already editing
        var inputStr = $('#output').val();
        if(inputStr.length > 0) {     //If there is text to edit
            editing = true;
            $("#output").removeAttr("disabled");    //Enable textarea
            $('#output').focus();   //Set cursor
            toggleButton(); //Show submit button
        }
        else
        {
            alert('No output to edit');
        }
    }
}

//Calls PostEditServlet with text in output box
function submitEdits() {
    if(changed) {   //If the text is changed
        $('body').append('<div id="progress">Loading...</div>');    //add loading div
        var dd = new Date();
        var totalTime = dd.getTime() - startTime;   //Calculates amount of time spent editing

        $.get('PostEditServlet', {edits: encodeURI($('#output').val()), time: totalTime, id: transid}, function(responseText) {
            $('#progress').remove();    //Remove loading div
            alert(responseText);    //Alert with server reply
        })
        .error(function() {
            $('#progress').remove();
            alert("Sorry, there was an error. Try again later.");
        });
    }
    else
        alert('Please make changes before submitting');
}

function makeMailto() {
    var inputStr = $('#output').val();
    if(inputStr.length > 0) {     //If there is text to edit
        var mailtoURL = "mailto:someone@somewhere.com?body=" + inputStr;
        location.href = mailtoURL;
        return true;
    }
    else {
        alert('No output to email.');
        return false;
    }
}

function makeSearch() {
    var inputStr = $('#output').val();
    if(inputStr.length > 0) {     //If there is text to edit
        var searchURL;
        //Order N, but there will never be many options
        for(var i = 0; i < searchInfo.length; i+=2) {
        //Search engine array has search engine name every second entry
            if($('#engine').val() == searchInfo[i])
                searchURL = searchInfo[i+1];    //Preceeding entry is the parameterless URI
        }
        searchURL = searchURL + encodeURI(inputStr);
        open(searchURL);
    }
    else {
        alert('No output to search for.');
    }
}


function refresh() {
	$('body').append('<div id="progress">Loading...</div>');
	history.go(0);
}

//Returns array with all duplicates removed
function getUniques(arrayName)
{
        var newArray=new Array();
label:  for(i=0; i<arrayName.length;i++ )
        {
            for(var j=0; j<newArray.length;j++ )
            {
                if(newArray[j]==arrayName[i])
                    continue label;
            }
            newArray[newArray.length] = arrayName[i];
        }
        return newArray;
}
