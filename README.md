# High Dynamic Range (HDR) - Exposure Fusion - Android 📸
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/AdarshRevankar/RenderScript)
[![](https://jitpack.io/v/AdarshRevankar/Exposure-Fusion.svg)](https://jitpack.io/#AdarshRevankar/Exposure-Fusion)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/AdarshRevankar/Exposure-Fusion/master/LICENSE.md)

---

### Deprecation ⚠️
This repository has been deprecated from Android 12 Beta
```
https://developer.android.com/about/versions/12/deprecations#renderscript
```

![logo](https://user-images.githubusercontent.com/48080453/84115484-c9046900-aa4b-11ea-80ff-4d81f0ed7dff.png)
![1](https://user-images.githubusercontent.com/48080453/84115561-e2a5b080-aa4b-11ea-9462-a167e2756d5e.png)
<br>
<br>
<br>
![2](https://user-images.githubusercontent.com/48080453/84115701-213b6b00-aa4c-11ea-9c71-59ff32bd80bf.png)
![3](https://user-images.githubusercontent.com/48080453/84115609-f3eebd00-aa4b-11ea-8f81-81981a237f41.gif)
![4](https://user-images.githubusercontent.com/48080453/84115757-37492b80-aa4c-11ea-94b0-df0cf3db0103.png)

### Exposure Fusion Algorithm

Library created for the demonstration of computation of HDR on Android Device. This library uses <b>RenderScript</b> extensively for the computation of pixels.

This Application is provided with beautiful interface as <b>Camera Activity</b> (optional), so as to provide the demonstration of the usage of the <b>HDR Library</b>.

Workflow of Exposure Fusion Algorithm is given below

[<img src="https://user-images.githubusercontent.com/48080453/83433018-dbccdb80-a456-11ea-9470-fe95e46d00eb.png"/>](flowchart.png)
---
### Structure
Structure of the hdr library is given below
```
    com.adrino.hdr
    |
    |-> corecamera
    |	|-> CameraActivity (c)
    |
    |-> corehdr
    |	|-> CreateHDR (c)
    |
    |-> Manager (c)

```
<i><b>NOTE:</b> Some of the Classes, Interfaces, Abstract Classes are not shown here. But in the back end it will be utilised. (c) indicates the classes </i>

---
### Install
Using the following steps you can include the `hdr` library in your Application

**Step 1. Add the JitPack repository to your build file**<br>
Add it in your root build.gradle at the end of repositories

```
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

**Step 2. Add the dependency**<br>
Provide the `latest_version` with the Release Versions ([![](https://jitpack.io/v/AdarshRevankar/Exposure-Fusion.svg)](https://jitpack.io/#AdarshRevankar/Exposure-Fusion) Get this version `HDRv...`)
```
dependencies {
    implementation 'com.github.AdarshRevankar:Exposure-Fusion:latest_version'
}
```

That's it! you have included the artifacts (jar, aar) into your project.

---
### Usage
Library is provided with the class `com.adrino.hdr.Manager` which consist of a single gateway method, which gives access to all the functionalities. We will try to inflate `CameraActivity` and try to produce `hdr` Image.
1. Import the class `Manager`, `CreateHDR` in the required activity.
    ```groovy
    import com.adrino.hdr.Manager;
    import com.adrino.hdr.corehdr.CreateHDR;
    ```
2. Create instance of `Manager` class, by passing through the Application Context to the Constructor
    ```groovy
    Manager hdrManager = new Manager(getApplicationContext());
    ```
    Manager consist of methods for creation of camera activity and HDR Image
    
| Method                                                              | What it does?                                                                                                                                                                                               |
|---------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `List<Bitmap> perform(List<Bitmap>, CreateHDR.Actions) `            | Returns the required  `Action` specified over List of images sent                                                                                                                                           |
| `void perform(Activity currActivity)`                               | Intents the CameraActivity over the current activity and provides the camera interface                                                                                                                      |
| `List<Bitmap> perform(Activity currActivity, boolean deleteImages)` | Returns the required  `Action`  specified over List of images sent by providing the CameraActivity. Provides boolean  `deleteImages` if you want to retain or delete the captured images (Security Reasons) |
| `List<Bitmap> getBmpImageList(File file)`                           | Returns the captured images from the Storage, when the External Storage location is given. For example,  `getExternalFilesDir(null)` is passed as the file parameter                                        |

3. Inflate the `CameraActivity` by calling `perform(Activity)` method
    ```groovy
    hdrManager.perform(this);
    ```
4. Performing `HDR` over the captured Images
	```groovy
    // Get the captured Images
    List<Bitmap> inputImageList = hdrManager.getBmpImageList(getExternalFilesDir(null));

    // Perform HDR Action over the Captured Images
    List<Bitmap> outputImageList = hdrManager.perform(inputImageList, CreateHDR.Actions.HDR);

    // Get the Bitmap HDR Image
    Bitmap hdrOutput = outputImageList.get(0);
    ```

---
### Things to be noted
`CreateHDR.Actions` is enumeration which has following items. Which is used to specify which intermediate result is required. Accordingly the size of output list varies.

| Action     | Description                 | inputImageList.size() | outputImageList.size()     |
|------------|-----------------------------|-----------------------|----------------------------|
| `HDR`      | Performs HDR                | 3                     | 1                          |
| `CONTRAST` | Edge Detection              | N                     | N                          |
| `EXPOSED`  | Detects Well Exposed Area   | N                     | N                          |
| `SATURATION`| Detects Vivid Colors        | N                     | N                          |
| `GAUSSIAN` | Gaussian Pyramid of Images  | N                     | N ( N- Pyramids, M-Levels) |
| `LAPLACIAN`| Laplacian Pyramid of Images | N                     | N ( N- Pyramids, M-Levels) |
| `RESULTANT`| Resultant of Images         | 3                     | 1 (1- Pyramid, M-Levels)   |

Here, Pyramids behave differently. So, we have to handle them carefully. `GAUSSIAN` will return `N` Pyramids each of size `M` ( `M` - Depends on the resolution of Image ). So, the `outputImageList` contains `N x M` Images.

---
If any error, suggestions please do contact
`Adarsh Revankar` adarsh_revankar@live.com
Enjoy learning ☕☕☕
