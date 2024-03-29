function HeatMap(opts) {
	this.config(opts);
}

HeatMap.prototype = {

	// 初始化参数配置
	config: function(opts){
		var me = this;
		var w = opts.width, h = opts.height, id = opts.id;
		me.options = {
			width: w,
			height: h,
			opacity: 100,
			radius: 10,
			bshadow: 30 / 20,
			boundVal: 15000,
			shadowBlur: 15,
			points: {
				max: 100,
				data: []
			},
			gradient: {0.45: "rgb(0,0,255)", 0.55: "rgb(0,255,255)", 0.65: "rgb(0,255,0)", 0.95: "yellow", 1.0: "rgb(255,0,0)"}
		};
		// 热图画布
        var container = document.getElementById(id);
        var canvas = document.createElement("canvas"), ctx = canvas.getContext('2d');
        canvas.width = w;
        canvas.height = h;
        container.appendChild(canvas);

		// 调色板画布
        var pcanvas = document.createElement("canvas"), pctx = pcanvas.getContext('2d');
        pcanvas.width = 1;
        pcanvas.height = 256;
        pcanvas.style.display = 'none';
        container.appendChild(pcanvas);
	
		me.options.ctx = ctx;
		me.options.pctx = pctx;
	},

    // 绘制圆阴影：可视区域只绘制了一个圆阴影效果，而圆本身的位置在可视区域之外
    renderShadow: function(x, y, val, cache) {
        var me = this,
            ctx = me.options.ctx,
            radius = me.options.radius,
        	bval = me.options.boundVal;

        var opacity = parseFloat(val / me.options.points.max, 10);
        ctx.shadowColor = ('rgba(0, 0, 0, ' + opacity + ')');
        ctx.shadowOffsetX = bval;
        ctx.shadowOffsetY = bval;
        ctx.shadowBlur = me.options.shadowBlur;
        ctx.beginPath();
        ctx.arc(x - bval, y - bval, me.options.radius, 0, Math.PI * 2, true);
        ctx.closePath();
        ctx.fill();
		if(!cache){
			me.cachePoint(x, y, val);
		}
    },

	// 绘制彩色热图根据调色板
    colorize: function() {
        var me = this, w = me.options.width, h = me.options.height, ctx = me.options.ctx;
        var img = ctx.getImageData(0, 0, w, h);
        var imgData = img.data, len = imgData.length, palette = me.getPalette();
        var opacity = me.options.opacity;
		
        for (var i = 3; i < len; i += 4) {
            // [0] -> r, [1] -> g, [2] -> b, [3] -> alpha
            var alpha = imgData[i],
			// 这里为什么乘以4，弄了好长时间才搞明白
			// 是因为要取调色板中的颜色，而调色板数的颜色数据对应的步长是4
			// 如果这里的4换成其它值，更有意思
                offset = alpha * 4;

            if (!offset){
                continue;
			}

            var finalAlpha = (alpha < opacity) ? alpha : opacity;
            imgData[i - 3] = palette[offset];
            imgData[i - 2] = palette[offset + 1];
            imgData[i - 1] = palette[offset + 2];
            imgData[i] = finalAlpha;
        }
        img.data = imgData;
        ctx.putImageData(img, 0, 0);
    },
	
    // 通过调色板来获取平滑的颜色值
    getPalette: function() {
        var me = this, gradient = me.options.gradient, pctx = me.options.pctx;
        //缓存调色板数据
        var grad = me.options.pctx.createLinearGradient(0, 0, 1, 256);
        for (var x in gradient) {
            grad.addColorStop(x, gradient[x]);
        }
        pctx.fillStyle = grad;
        pctx.fillRect(0, 0, 1, 256);
		// 这个了有说明: http://www.w3school.com.cn/tags/canvas_getimagedata.asp
		// 返回的是一个一位数组，每一个像素的的颜色有四个值来表示
		// 前三个值表示 红绿蓝，第四个值表示alpha 通道
		// 也就是这个一位数组的长度是: 1 * 256 * 4;
        return pctx.getImageData(0, 0, 1, 256).data;
    },

}

// 缓存点数据
HeatMap.prototype.cachePoint = function(x, y, val){
	var me = this, points = me.options.points, data = points.data;
	if(val > points.max){
		points.max = val;
	}
	data.push([x, y, val]);
}

// 增加一个热点
HeatMap.prototype.addPoint = function(x, y, val){
	var me = this;
	me.options.ctx.clearRect(0, 0, me.options.width, me.options.height);
	me.options.pctx.clearRect(0, 0, 1, 256);

	var data = me.options.points.data, len = data.length;
	for(var i = 0; i < len; i ++){
		me.renderShadow(data[i][0], data[i][1], data[i][2], true);
	}
	me.renderShadow(x, y, val);
	me.colorize();
}

// 随机画热点
HeatMap.prototype.randomPoints = function(points){
	var me = this;
	var w = me.options.width, h = me.options.height, max = 200;
	for(var i = 0; i < points; i ++){
		me.renderShadow(Math.floor(Math.random() * w + 1 ), Math.floor(Math.random() * h + 1 ), Math.floor(Math.random()* max + 1));
	}
	me.colorize();
}

