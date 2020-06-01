# High Dynamic Range (HDR) - Android
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/AdarshRevankar/RenderScript)
[![version](https://img.shields.io/badge/version-1.2.0-yellow.svg)](https://semver.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/Arpith-kumar/DsDojo/blob/master/LICENSE.md)
### Exposure Fusion Algorithm

Library created for the demonstration of computation of HDR on Android Device. This library uses <b>RenderScript</b> extensively for the computation of pixels.

This Application is provided with beautiful interface as <b>Camera Activity</b> (optional), so as to provide the demonstration of the usage of the <b>HDR Library</b>.

Worflow of Exposure Fusion Algorithm is given below

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
In Android Studio devs can import the `.aar` module from the release build / can include the library ```hdr``` to their project.

1. Open `Project Structure > All Dependencies > +` select the `.aar` file
2. Import the module to the current project & Apply

---
### Usage
Library is provided with the class `com.adrino.hdr.Manager` which consist of a single gateway method, which gives access to all the functionalities. We will try to inflate `CameraActivity` and try to produce `hdr` Image.
1. Import the class `Manager`, `CreateHDR` in the required activity.
    ```
    import com.adrino.hdr.Manager;
    import com.adrino.hdr.corehdr.CreateHDR;
    ```
2. Create instance of `Manager` class, by passing through the Application Context to the Constructor
    ```
    Manager hdrManager = new Manager(getApplicationContext());
    ```
    Manager consist of methods for creation of camera activity and HDR Image
    1. `List<Bitmap> perform(List<Bitmap>, CreateHDR.Actions)` - Returns the required `Action` specified over List of images sent.
    2. `void perform(Activity currActivity)` - Intents the CameraActivity over the current activity and provides the camera interface.
    3. `List<Bitmap> perform(Activity currActivity, boolean deleteImages)` - Returns the required `Action` specified over List of images sent by providing the CameraActivity. Provides boolean `deleteImages` if you want to retain or delete the captured images (Security Reasons).
    4. `List<Bitmap> getBmpImageList(File file)` - Returns the captured images from the Storage, when the External Storage location is given. For example, `getExternalFilesDir(null)` is passed as the file parameter.
3. Inflate the `CameraActivity` by calling `perform(Activity)` method
    ```
    hdrManager.perform(this);
    ```
4. Performing `HDR` over the captured Images
	```
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
### Snapshots
Samples of our project is shown below

|[<img src="https://user-images.githubusercontent.com/48080453/83431542-1b92c380-a455-11ea-87bc-5004a7eb3bd0.png"/>](splash.png)<br>Splash Screen|  [<img src="https://user-images.githubusercontent.com/48080453/83431821-94921b00-a455-11ea-84df-fcfccb1cc391.png"/>](camera.png)<br>Camera View | [<img src="https://user-images.githubusercontent.com/48080453/83431996-d1f6a880-a455-11ea-9539-c98843f77cf7.png"/>](contrast.png)<br>Contrast  |
|----------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
|[<img src="https://user-images.githubusercontent.com/48080453/83432026-dae77a00-a455-11ea-9493-5a55cf443a9f.png"/>](exposure.png)<br>Exposure|[<img src="https://user-images.githubusercontent.com/48080453/83432054-e3d84b80-a455-11ea-96bb-0af997f7c49b.png"/>](saturation.png)<br>Saturation|[<img src="https://user-images.githubusercontent.com/48080453/83432063-e89cff80-a455-11ea-9d65-06af6edda882.png"/>](normal.png)<br>Normal|

|[<img src="https://user-images.githubusercontent.com/48080453/83432261-3dd91100-a456-11ea-902f-9fc5081a071a.png"/>](gaussian.png)<br>Gaussian Pyramid|[<img src="https://user-images.githubusercontent.com/48080453/83432263-3e71a780-a456-11ea-8541-7d47ea8f8403.png"/>](laplacian.png)<br>Laplacian Pyramid|[<img src="https://user-images.githubusercontent.com/48080453/83432266-3fa2d480-a456-11ea-9952-f25ceeeb2830.png"/>](resultant.png)<br>Resultant Pyramid|

---
### Contact
If any error, suggestions please do contact :

`Adarsh Revankar` adarsh_revankar@live.com
