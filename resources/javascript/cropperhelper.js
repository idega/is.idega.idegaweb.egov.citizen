jQuery(document).ready(function() {
	jQuery(".iw-image-layer > img").cropper({
		aspectRatio : eval(jQuery('#img-aspectRatio').val()),
		preview : ".iw-image-preview",
		crop : function(data) {
			jQuery("#img-dataX").val(Math.round(data.x));
			jQuery("#img-dataY").val(Math.round(data.y));
			jQuery("#img-dataHeight").val(Math.round(data.height));
			jQuery("#img-dataWidth").val(Math.round(data.width));
		}
	});
});