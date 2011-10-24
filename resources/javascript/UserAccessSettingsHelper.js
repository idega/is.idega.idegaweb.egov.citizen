/**
 * 
 */
var UserAccessSettingsHelper = {};

jQuery(document).ready(function(){
	UserAccessSettingsHelper.initialize();
});

UserAccessSettingsHelper.saveAccessSettings = function(selector){
	showLoadingMessage("");
	var form = jQuery(selector);
	var parameters = form.serializeArray();
	
	var map = {};
	for(var i = 0;i < parameters.length;i++){
		var element = parameters[i];
		if(map[element.name] == undefined){
			map[element.name] = [];
		}
		map[element.name].push(element.value);
	}
	humanMsg.displayMsg(map);
	CitizenServices.saveUserAccessSetings(map,{
		callback: function(reply){
			closeAllLoadingMessages();
			humanMsg.displayMsg(reply);
		}
	});
}
UserAccessSettingsHelper.addLanguage = function(formSelector,selectSelector,containerSelector,paramName,imageUrl){
	var elements = jQuery(formSelector).find(selectSelector).children(":selected");
	if(elements.length == 0){
		return;
	}
	var languages = {};
	for(var i = 0;i < elements.length;i++){
		var element = jQuery(elements[i]);
		var info = {};
		info.text = element.text();
		info.element = element;
		languages[element.val()] = info;
	}
	UserAccessSettingsHelper.addLanguages(formSelector,containerSelector,languages,paramName,imageUrl);
}

// For some reason did not worked without data
UserAccessSettingsHelper.LANGUAGE_DATA_NAME = "language-data";
UserAccessSettingsHelper.addLanguages = function(formSelector,containerSelector,languages,paramName,imageUrl){
	var container = jQuery(formSelector).find(containerSelector);
	var elements = container.find("[name ="+ paramName +"]");
	var values = [];
	if(elements.length > 0){
		for(var i = 0;i<elements.length;i++){
			values.push(values.push(jQuery(elements[i]).val()));
		}
	}
	
	for (var key in languages) {
		if(jQuery.inArray(key, values) != -1){
			continue;
		}
		var row = jQuery("<tr/>");
		container.append(row);
		var div = jQuery("<td/>");
		row.append(div);
		var input = jQuery("<input type = 'hidden'/>");
		div.append(input);
		if(paramName != null){
			input.attr("name",paramName);
		    input.val(key);
		}
		var info = languages[key];
		div.append(languages[key].text);
		div = jQuery("<td/>");
		row.append(div);
		var deleteImg = jQuery("<img src='"+ imageUrl +"'/>");
		div.append(deleteImg);
		info.element.hide();
		info.row = row;
		deleteImg.data(UserAccessSettingsHelper.LANGUAGE_DATA_NAME,info);
		deleteImg.click(function(){
			var data = jQuery(this).data(UserAccessSettingsHelper.LANGUAGE_DATA_NAME);
			data.row.remove();
			data.element.show();
		});
	}
}

UserAccessSettingsHelper.initLanguages = function(selectSelector,containerSelector,userId,languages,imgSrc){
	var select = jQuery(selectSelector);
	var container = jQuery(containerSelector);
	for(var i = 0;i<languages.length;i++){
		var element = select.find("[value = "+languages[i]+"]");
		var row = jQuery("<tr/>");
		container.append(row);
		var div = jQuery("<td/>");
		row.append(div);
		
		var info = {};
		info.element = element;
		info.languageId = languages[i];
		element.hide();
		info.row = row;
		div.append(element.text());
		div = jQuery("<td/>");
		row.append(div);
		var deleteImg = jQuery("<img src='"+ imgSrc +"'/>");
		div.append(deleteImg);
		deleteImg.data(UserAccessSettingsHelper.LANGUAGE_DATA_NAME,info);
		deleteImg.click(function(){
			var data = jQuery(this).data(UserAccessSettingsHelper.LANGUAGE_DATA_NAME);
			CitizenServices.removeLanguage(userId,data.languageId, {
				callback: function(removed){
					if(!removed){
						humanMsg.displayMsg(UserAccessSettingsHelper.FAILED_MSG);
						return false;
					}
					data.row.remove();
					data.element.show();
					closeAllLoadingMessages();
					humanMsg.displayMsg(UserAccessSettingsHelper.REMOVE_MSG);
				}
			});
		});
	}
}

//jQuery(document).ready(function(){
//	var UserAccessSettingsHelper.languages = [];
//	UserAccessSettingsHelper.languages[0] = '1';
//	UserAccessSettingsHelper.initLanguages('#iwidb9bdf26da68c',
//			'#iwid5151ad4fa4f4',
//			'2',
//			UserAccessSettingsHelper.languages,
//			'/idegaweb/bundles/is.idega.idegaweb.egov.citizen/resources/delete.png');
//});
