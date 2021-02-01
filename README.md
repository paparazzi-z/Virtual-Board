This is the project of group PRINFO IM4.

Members : BICHICHI, BREGNAC, FERNANDO, ZHOU, BLANQUINQUE.


21012021
- Edit configuration phase, white Mask on object when showing the frame camera
- Added save the draw with background or not
- Added the possibility to change the webcam camera
- Fixing eraser disk size (the eraser just erased without filling the circle)
- Fixing deleting files on the good path directory


20012021
- Added a frame to see the Mask and aswell the current camera frame to know if we are tracking the object or not
- Added a delete sheet button to delete sheet from the list and also the local file with confirmation box
- Added labels for the HSV ranges sliders
- Fix the saving functionnality (problem with already saved or not sheet)
- Create a directory named 'Dessins' to save all our drawings


06012021
- Added manual functionnality and button to draw manually
- Added the eventhandler for the manual draw
- Fixed a problem when the object is not detected, it draws the last point 


03012021
- Added features for the configuration phase
 A label added which says ('Configuration phase')
 Sliders are added to adjust the HSV Threesolding
- The Brightness function has been added, if we don't have enough bright we can't start the configuration of object
- Fixing the jpg to png format for saving
- Return the Mask during the configuration phase when the object is chosen, adjust with sliders for the HSV borders
- Fixing some problems for NoiseRemover function 
- Draw a circle around the object and no more using moments method but minEnclosingCircle to find the center of object



02012021
- Added a slider to choose size of thickness of pencil or eraser
- Added eventhandler for slider, colorpicker and Listview(change draw sheets)
- Fix displaying save confirmation box if image not already save to get filename
- Display 'no drawing' when the object is not detected
- Fixing erasing functionnalityn, and we can also choose the eraser size



30122020 
- Added Colorpicker to change colors (default is white)
- Added Erasing button to eraser
- Added a listsheet to change and view all sheets and also newdraw now add the draw to the list
- Added a label to communicate better with the user
- Added the saving image button
- Added a GaussianBlur and a flip image to the frame
- Drawing in the good sheet in the list
- Fix object not detected when we dont find any points



28122020 
- Drawing on the a sheet (only one)
- Added sheet functionnality (new draw button)


21112020 
-Added Mousevent to detect the point clicked on the frame - used for configuration phase

15112020 
- Open Camera and show the frame 30fps
- Added HSVRangeDeterminator
- Drawing with contours

PS for other members : don't forget to add required libraries in Eclipse for this to work ( JavaFX , OpenCV & JavaFX SDK)