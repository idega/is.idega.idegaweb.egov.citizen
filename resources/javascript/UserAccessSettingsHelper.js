/**
 * 
 */
var UserAccessSettingsHelper = {};

jQuery(document).ready(function(){
	UserAccessSettingsHelper.initialize();
});

UserAccessSettingsHelper.saveAccessSettings = function(selector,loginsLayerSelector,userId){
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
	
	var singleSingOnLayer = jQuery(loginsLayerSelector);
	var logins = [];
	var loginlayers = singleSingOnLayer.children();
	for(var i = 0;i<loginlayers.length;i++){
		var layer = jQuery(loginlayers[i]);
		var data = {};
		data.loginId = layer.find(UserAccessSettingsHelper.SINGLE_SING_ON_ID_SELECTOR).val();
		data.address = layer.find(UserAccessSettingsHelper.SINGLE_SING_ON_SERVER_SELECTOR).val();
		data.userName = layer.find(UserAccessSettingsHelper.SINGLE_SING_ON_NAME_SELECTOR).val();
		data.password = layer.find(UserAccessSettingsHelper.SINGLE_SING_ON_PASSWORD_SELECTOR).val();
		logins.push(data);
	}
	
	CitizenServices.saveUserAccessSetings(map,logins,{
		callback: function(reply){
			closeAllLoadingMessages();
			humanMsg.displayMsg(reply);
			reloadPage();
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


UserAccessSettingsHelper.addSingleSingOn = function(layerSelector){
	if(UserAccessSettingsHelper.SINGLE_SING_ON_NEW_LAYER != undefined){
		jQuery(layerSelector).append(UserAccessSettingsHelper.SINGLE_SING_ON_NEW_LAYER);
		return;
	}
	showLoadingMessage("");
	CitizenServices.getSingleSingOnLayer({
		callback: function(layer){
			UserAccessSettingsHelper.SINGLE_SING_ON_NEW_LAYER = layer;
			jQuery(layerSelector).append(layer);
			closeAllLoadingMessages();
		}
	});
}
UserAccessSettingsHelper.removeSingleSingOn = function(layerSelector,loginId){
	showLoadingMessage("");
	if(loginId == '-1'){
		jQuery(layerSelector).remove();
		closeAllLoadingMessages();
		return;
	}
	CitizenServices.removeSingleSingOn(loginId,{
		callback: function(removed){
			if(removed){
				jQuery(layerSelector).remove();
				closeAllLoadingMessages();
				humanMsg.displayMsg(UserAccessSettingsHelper.REMOVE_MSG);
				return;
			}
			humanMsg.displayMsg(UserAccessSettingsHelper.FAILED_MSG);
			closeAllLoadingMessages();
		}
	});
}
