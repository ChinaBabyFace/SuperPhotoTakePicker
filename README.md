# SuperPhotoTakePicker
Android 一个简单的头像选择工具 拍照 或者 从相册选择 裁切 最终拿到裁切后的文件，使用者可以调用自己的接口上传到自己的服务器，实现类似于头像上传的功能
[ ![Download](https://api.bintray.com/packages/chinashark/maven/superphototakepicker/images/download.svg) ](https://bintray.com/chinashark/maven/superphototakepicker/_latestVersion)
## 特点
1.基于Android原生提供的接口，没有自建图片选择器，图片裁剪器及相机，大幅度降低适配难度  
2.适配Android7.0及以上  
3.包的体积非常小,代码简单，自定义容易
4.通过配置，可以只拿到相机或相册用户选择的照片而不裁切

## Feature
1.Base on android api to camera take photo,album pick photo,crop photo.No third part jar.  
2.Adapt Android 7.0+.  
3.SuperPhotoTakePicker Jar is very small,very simple,custom easy.
4.Set param，you can get raw photo from camera or album,no crop.

## 使用方法 USE
1.添加gradle依赖 Add gradle dependencies
```gradle
implementation 'com.shark:superphototakepicker:1.0.2'
```
2.由于Android7.0的原因在Manifest中创建自己的FileProvider.Create FileProvider in Mainfest

如果有的话就不需要创建了，注意authorities的值，这里是本APP的包名，可以用其他名.
Need not create，if you have file provideer.Notice authorities value.You can use packageName or other string
```xml
 <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="${applicationId}"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
  </provider>
```
3.设置Picker  Setting picker
```java
SuperPhotoTakePicker picker = new SuperPhotoTakePicker.Builder(this)
                .setAspectX(1)//设置裁剪后图片的横纵比例,x/y,square or rectangle
                .setAspectY(1)//设置裁剪后图片的横纵比例
                .setCropPhoto(true)//if false callback onRawPhotoReceived,if true callback onCropPhotoReceived
                .setCompressFormat(Bitmap.CompressFormat.JPEG)//裁剪后图片的存储格式，,crop photo format
                .setCropWidth(200)//裁剪后图片的大小,crop photo width
                .setCropHeight(200)//裁剪后图片的大小,,crop photo height
                .setAuthority(getPackageName())//本APP的FileProvider的Authority,use step 2 authorities value
                .setOnPhotoChangedListener(this)//callback
                .create();
```
