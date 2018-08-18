# Lximagegetter
 <br />
图片加载框架，主要解决： <br />
1.大图加载问题。 <br />
2.图片过多内存溢出问题。 <br />
3.图片显示动画。 <br />
4.列表图片加载错位问题。 <br />

引入： <br />
allprojects { <br />
		repositories { <br />
			... <br />
			maven { url 'https://jitpack.io' } <br />
		} <br />
} <br />
dependencies { <br />
	        implementation 'com.github.flappygod:Lximagegetter:1.1' <br />
} <br />
 <br />
 <br />
初始化： <br />
//初始化图片加载框架 <br />
LXImageGetter.init(getApplicationContext(), 8, BaseConfig.imagePath, 10, 10); <br />



使用: <br />
LXImageGetter.getInstance().getImageWithUrl(ImageView,url); <br />
