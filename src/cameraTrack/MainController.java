package cameraTrack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.videoio.VideoCapture;

import Utils.Utils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;

public class MainController {
	// the FXML button
	@FXML
	private Button btn_str;
	// the FXML image view
	@FXML
	private ImageView currentFrame;

	// the sheet button
	@FXML
	private Button btn_gomme;

	// the sheet button
	@FXML
	private Button btn_ndraw;
	
	// the deleting sheet button
	@FXML
	private Button deletesh;

	// the label to display messages
	@FXML
	private Label label1;
	
	// the label to display configuration phase
	@FXML
	private Label tlabel;

	// the list to display the list of sheets
	@FXML
	private ListView<String> listsheet;
	
	// the button to end configuration phase
	@FXML
	private Button btn_endConfig;

	// the button to save the image
	@FXML
	private Button btn_saveim;
	
    // the button to change to manuel mode
	@FXML
	private Button manuel;
	
	// the button to change the camera to internal or external
	@FXML
	private Button extcamera;
	
	// the checkbox to save or not the background with the drawing
	@FXML
	private CheckBox savebackground;
	
	ObservableList<String> list = FXCollections.observableArrayList();

	// the ColorPicker
	@FXML
	private ColorPicker colorpicker;

	// the slider for thickness
	@FXML
	private Slider slider;
	
	// the slider for HSV borders for configuration
	@FXML
	private Slider HminSld;
	@FXML
	private Slider HmaxSld;
	@FXML
	private Slider SminSld;
	@FXML
	private Slider SmaxSld;
	@FXML
	private Slider VminSld;
	@FXML
	private Slider VmaxSld;
	@FXML
	private Label Hminlbl;
	@FXML
	private Label Hmaxlbl;
	@FXML
	private Label Sminlbl;
	@FXML
	private Label Smaxlbl;
	@FXML
	private Label Vminlbl;
	@FXML
	private Label Vmaxlbl;
	
	// Different Logos
	@FXML
	private ImageView sliderLogo;
	@FXML
	private ImageView gommeLogo;
	@FXML
	private ImageView gommeLogo1;
	@FXML
	private ImageView ndrawLogo;
	@FXML
	private ImageView saveimLogo;
	@FXML
	private ImageView colorpickerLogo;
	@FXML
	private ImageView WebcamLogo1;
	@FXML
	private ImageView WebcamLogo2;
	@FXML
	private ImageView manuelLogo;
	
	
	// For follow up rate elements
	@FXML
	private CheckBox followRate;
	
	@FXML
	private Label followRateNbr;
	
	
	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// the OpenCV object that realizes the video capture
	private VideoCapture capture = new VideoCapture();
	// a flag to change the button behavior
	private boolean cameraActive = false;
	
	// the id of the camera to be used
	private static int cameraId = 0;
	// Bool to check if we have already run configuration phase 
	private boolean configurationphase = false;
		
	private ArrayList<Point> coord = new ArrayList<Point>();

	// The threshold to use
	private Scalar lowestThresh;
	private Scalar highestThresh;
	
	// The boolean to know if there is enough brightness
	private boolean brightness = false;

	// Drawing points for line
	private ArrayList<Point> points = new ArrayList<Point>();;

	// Drawing sheets
	private ArrayList<Mat> drawings = new ArrayList<Mat>();

	// Boolean to know if we are drawing or not manually
	private boolean modemanuel = false;

	// Boolean to know if we are drawing or not
	private boolean gommebl = false;

	// Indice of the sheet
	private int sheetind = 0;

	// Color of the paint
	private Scalar color = new Scalar(255, 255, 255); // color white

	// Thickness of the paint
	private double thickness = 3;
	
	// To calculate the follow up rate.
	int nbrImageDetected = 0;
	int nbrImageTotal = 1;


	/**
	 * The action triggered by pushing the button on the GUI
	 *
	 * @param event the push button event
	 */
	@FXML
	protected void startCapture(ActionEvent event) {
		if (!cameraActive) {
			// Hide camera changing 
			extcamera.setVisible(false);
			WebcamLogo1.setVisible(false);
			WebcamLogo2.setVisible(false);
			
			// start the video capture
			capture.open(cameraId);
			
			if (!configurationphase) {
				tlabel.setText("Configuration Phase");
			}
			
			// is the video stream available?
			if (capture.isOpened()) {
				cameraActive = true;


				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {

					@Override
					public void run() {

						// effectively grab and process a single frame
						Mat frame = grabFrame();

						// convert and show the frame
						Mat dst = frame;

						if (drawings.size() != 0) {
							Core.addWeighted(frame, 1, drawings.get(sheetind), 1, 1, dst);
						}

						Image imageToShow = Utils.mat2Image(dst);

						updateImageView(currentFrame, imageToShow);
						
						// We calculate total nbr of images
						if(followRate.isSelected()) {
							nbrImageTotal += 1;
							double flwRate =  ((double)nbrImageDetected/(double)nbrImageTotal)*100;
							System.out.println("nbr total : "+nbrImageTotal);
							System.out.println("Ratio : "+(int)flwRate);
							
							Platform.runLater(() -> {
								// update the label for follow up rate
								followRateNbr.setText(String.valueOf((int)flwRate)+" % ");
							});
						}
					}
				};

				// Create EventHandler for getting the points after clicking for the thresold
				currentFrame.addEventFilter(MouseEvent.MOUSE_CLICKED, eventHandlerclick);
				// Create EventHandler for getting the points after dragging for manual drawing
				currentFrame.addEventFilter(MouseEvent.MOUSE_DRAGGED, eventHandlerpress);


				// Create EventHandler for colors
				colorpicker.setOnAction((ActionEvent e) -> {
					color = Utils.toRGBCode(colorpicker.getValue());

				});
				
				// Create EventHandler for slider 
				slider.setOnMouseReleased(event2 -> {
					thickness = slider.getValue();
		        });
				
				listsheet.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
				
				// Create EventHandler for Listview select the good sheet
				listsheet.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
					 
		            @Override
		            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		                if ((int)newValue>=0) {
		                	sheetind = (int)newValue;
		                }
		            	
		            }
		 
		             
		        });

				timer = Executors.newSingleThreadScheduledExecutor();
				timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				

				// update the button content
				btn_str.setText("Stop Camera");
			} else {
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		} else {
			// the camera is not active at this point
			cameraActive = false;
			// update again the button content
			btn_str.setText("Start Camera");
			

			// stop the timer
			stopAcquisition();
		}
	}

	/**
	 * The action triggered by pushing the button DrawSheet on the GUI, creating a
	 * new drawsheet
	 *
	 * @param event the push button event
	 */
	@FXML
	protected void newdraw(ActionEvent event) {

		if (this.highestThresh != null) {

			this.drawings.add(Mat.zeros(480, 640, CvType.CV_8UC3));
			this.sheetind = drawings.size() - 1;

			String s = String.valueOf(drawings.size()) + " (not saved) ";

			list.add(s);

			listsheet.getItems().clear();
			listsheet.getItems().addAll(list);

		}

	}

	/**
	 * The action triggered by pushing the button manuel pass to the manuel mode
	 *
	 * @param event the push button event
	 */
	@FXML
	protected void manuelmode(ActionEvent event) {

		if (!modemanuel) {

			modemanuel = true;
			manuel.setText("Mode auto");
			

		} else {
			modemanuel = false;
			manuel.setText("Mode manuel");
		}

	}
	
	
	/**
	 * The action triggered by pushing the button manuel pass to the manuel mode
	 *
	 * @param event the push button event
	 */
	@FXML
	protected void changecam(ActionEvent event) {

		if (cameraId==0) {

			this.cameraId = 1;
			extcamera.setText("Internal camera");
			

		} else {
			this.cameraId = 0;
			extcamera.setText("External camera");
		}

	}
	/**
	 * Save the current drawing image
	 *
	 * @param event the push button event
	 */

	@FXML
	protected void saveim(ActionEvent event) {
		

		if (drawings.size()!=0) {
			
			String file = "";
			
			 File directory = new File("Dessins");
			   if (! directory.exists()){
			        directory.mkdir();
			   }
			
			if (listsheet.getItems().get(sheetind).contains("not saved")) {
				
				TextInputDialog dialog = new TextInputDialog(String.valueOf(sheetind + 1));
				dialog.setTitle("Image Saving");
				dialog.setContentText("Please enter the filename :");



				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					file = result.get();
					
					list.set(sheetind, file);
					listsheet.getItems().clear();
					listsheet.getItems().addAll(list);

					
					String file2 = System.getProperty("user.dir") + '\\'+ "Dessins"+ '\\'+file + ".png";
					
					if (savebackground.isSelected()) {
						
						Mat frame = new Mat();
						capture.read(frame);
						Core.flip(frame, frame, +1);
						
						Mat dst = new Mat();
						Core.addWeighted(frame, 1, this.drawings.get(sheetind), 1, 1, dst);
						Imgcodecs.imwrite(file2, dst);
						
					} else {
						Imgcodecs.imwrite(file2, this.drawings.get(sheetind));
					}

						System.out.println("Image Saved ...");
					
				} 
				
			} else {
				file = listsheet.getItems().get(sheetind);
				
				String file2 = System.getProperty("user.dir") + '\\'+ "Dessins"+ '\\'+file + ".png";
				
				if (savebackground.isSelected()) {
					
					Mat frame = new Mat();
					capture.read(frame);
					Core.flip(frame, frame, +1);
					
					Mat dst = new Mat();
					Core.addWeighted(frame, 1, this.drawings.get(sheetind), 1, 1, dst);
					Imgcodecs.imwrite(file2, dst);
					
				} else {
					Imgcodecs.imwrite(file2, this.drawings.get(sheetind));
				}
				
				System.out.println("Image Saved ...");
			}

			

		} else {
			Platform.runLater(() -> {
				label1.setText("No drawing sheet to save.");	
			});
		}
		
	}

	/**
	 * Erasing component drawing
	 *
	 * @param event the push button event
	 */

	@FXML
	protected void gomme(ActionEvent event) {
		if (drawings.size()!=0) {
			if (!gommebl) {
				points.clear();
				gommebl = true;

				Platform.runLater(() -> {
					btn_gomme.setText("Draw");
					gommeLogo1.setVisible(true);
					gommeLogo.setVisible(false);
				});

			} else {
				gommebl = false;
				points.clear();

				Platform.runLater(() -> {
					btn_gomme.setText("Erase");
					gommeLogo1.setVisible(false);
					gommeLogo.setVisible(true);
				});

			}
		}
		else {
			Platform.runLater(() -> {
				label1.setText("No drawing sheet...");	
			});
		}

	}
	
	
	/**
	 * Deleting current drawing sheet and its file
	 *
	 * @param event the push button event
	 */

	@FXML
	protected void deletesheet(ActionEvent event) {
		
		if (drawings.size()==0) {
			Platform.runLater(() -> {
				label1.setText("No drawing sheet...");	
			});
		} else {
			int opt = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete including the file","Delete", JOptionPane.YES_NO_OPTION);
			if (opt==0) {
				String filename = System.getProperty("user.dir") + '\\'+ "Dessins"+ '\\'+listsheet.getItems().get(sheetind) + ".png";
				File myfile = new File(filename); 
			    
				if (myfile.delete()) { 
			      System.out.println("Deleted the file: " + myfile.getName());
			    } else {
			      System.out.println("Failed to delete the file.");
			    } 
				
				list.remove(sheetind);
				listsheet.getItems().clear();
				listsheet.getItems().addAll(list);
				drawings.remove(sheetind);
				if (sheetind>0) {
					sheetind=sheetind-1;
				} else {
					sheetind=0;
				}
				
			}
			
		}
	}
	
	

	/**
	 * Get a frame from the opened video stream (if any)
	 *
	 * @return the {@link Mat} to show
	 */
	private Mat grabFrame() {
		
		Mat frame = new Mat();
		capture.read(frame);
		
		// This part of code blurs the image in case the camera introduces some noise.
		Imgproc.medianBlur(frame, frame, 5);
		Core.flip(frame, frame, +1); // Flip horizontaly the image (miror)
		
		if(!configurationphase && this.highestThresh == null) {
			checkBrightness(frame);
		}
		

		if (coord.size() == 1 && brightness) {

			try {

				Mat frameini = new Mat();
				capture.read(frameini);

				Imgproc.medianBlur(frameini, frameini, 5);
				Core.flip(frameini, frameini, +1); // Flip horizontaly the image (miror)

				Imgproc.GaussianBlur(frameini, frameini, new Size(15, 15), 0);

				Thread.sleep(3000);

				Mat frameHSVini = new Mat();

				Imgproc.cvtColor(frameini, frameHSVini, Imgproc.COLOR_BGR2HSV);

				Rect rectCrop = new Rect((int) coord.get(0).x, (int) coord.get(0).y,
						(int) coord.get(1).x - (int) coord.get(0).x, (int) coord.get(1).y - (int) coord.get(0).y);
				// Get ROI
				Mat imageROI = frameHSVini.submat(rectCrop);

				this.lowestThresh = HSV_RangeDeterminator(imageROI).get(0);
				this.highestThresh = HSV_RangeDeterminator(imageROI).get(1);
				
				HminSld.setVisible(true);
				Hminlbl.setVisible(true);
				SminSld.setVisible(true);
				Sminlbl.setVisible(true);
				VminSld.setVisible(true);
				Vminlbl.setVisible(true);
				
				HmaxSld.setVisible(true);
				Hmaxlbl.setVisible(true);
				SmaxSld.setVisible(true);
				Smaxlbl.setVisible(true);
				VmaxSld.setVisible(true);
				Vmaxlbl.setVisible(true);
				
				btn_endConfig.setVisible(true);
				
				Platform.runLater(() -> {
					label1.setText("Please adjust the borders if needed");	
				});
				
				

			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} else if (coord.size() == 1 && !brightness) {
			coord.clear();
		}

		// check if the capture is open
		if (capture.isOpened()) {
			try {

				// To end the configuration phase
				if(configurationphase == false && this.highestThresh != null) {
					
					this.lowestThresh = new Scalar(HminSld.getValue(),SminSld.getValue(),VminSld.getValue());
			        this.highestThresh = new Scalar(HmaxSld.getValue(),SmaxSld.getValue(),VmaxSld.getValue());

			        
					Mat Processframe = new Mat();
					Imgproc.GaussianBlur(frame, Processframe, new Size(15, 15), 0);

					Mat Mask = HSVThresholder(Processframe);
					
					btn_endConfig.setOnMouseClicked(ev -> {
					// Configuration phase is over whe can start drawing and showing the buttons
						configurationphase = true;
						
						
						HminSld.setVisible(false);
						Hminlbl.setVisible(false);
						SminSld.setVisible(false);
						Sminlbl.setVisible(false);
						VminSld.setVisible(false);
						Vminlbl.setVisible(false);
						
						HmaxSld.setVisible(false);
						Hmaxlbl.setVisible(false);
						SmaxSld.setVisible(false);
						Smaxlbl.setVisible(false);
						VmaxSld.setVisible(false);
						Vmaxlbl.setVisible(false);
						
						btn_ndraw.setVisible(true);
						ndrawLogo.setVisible(true);
						btn_saveim.setVisible(true);
						saveimLogo.setVisible(true);
						btn_gomme.setVisible(true);
						gommeLogo.setVisible(true);
						colorpicker.setVisible(true);
						colorpickerLogo.setVisible(true);
						slider.setVisible(true);
						sliderLogo.setVisible(true);
						manuel.setVisible(true);
						manuelLogo.setVisible(true);
						deletesh.setVisible(true);
						savebackground.setVisible(true);
						
						followRate.setVisible(true);
						followRateNbr.setVisible(true);
						
						btn_endConfig.setVisible(false);
						
						Platform.runLater(() -> {
							tlabel.setText("");	
						});
					});

					Mat maskrgb = new Mat();
					Mat framemask = new Mat();
					
					Imgproc.cvtColor(Mask,maskrgb,Imgproc.COLOR_GRAY2RGB);
					
					Core.addWeighted(frame, 1, maskrgb, 1, 1, framemask);

					return framemask;
				
				}
				
				// read the current frame

				// if the frame is not empty, process it

				

				if (this.highestThresh != null && drawings.size() != 0 && !modemanuel) {

					Mat Processframe = new Mat();
					Imgproc.GaussianBlur(frame, Processframe, new Size(15, 15), 0);

					Mat Mask = HSVThresholder(Processframe);

					Mat currentdraw = this.drawings.get(sheetind);

					DrawWithContours(Mask, currentdraw,frame);
					this.drawings.set(sheetind, currentdraw);

					
				} else if (this.highestThresh != null && drawings.size() == 0) {
					Platform.runLater(() -> {
						label1.setText("Ready to track, create a new draw sheet");
					});
					} else if (modemanuel) {
					Platform.runLater(() -> {
						label1.setText("Manuel draw activated, you can draw or erase manually");
					});
				}

			}

			catch (Exception e) {
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}

		return frame;
	}

	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	private void stopAcquisition() {
		if (timer != null && !timer.isShutdown()) {
			try {
				// stop the timer
				timer.shutdown();
				timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if (capture.isOpened()) {
			// release the camera
			capture.release();
		}
	}

	// Function to apply HSV Thresholder
	private Mat HSVThresholder(Mat frameToProcess) {

		Mat frameHSV = new Mat();
		Imgproc.cvtColor(frameToProcess, frameHSV, Imgproc.COLOR_BGR2HSV);

		Mat frameBW = new Mat();

		Core.inRange(frameHSV, this.lowestThresh, this.highestThresh, frameBW);

		frameBW = NoiseRemove(frameBW);

		return frameBW;
	}

	private Mat NoiseRemove(Mat frameToProcess) {

		Mat frameCl = new Mat();
		int elementType1 = Imgproc.CV_SHAPE_ELLIPSE;
		int elementType2 = Imgproc.CV_SHAPE_RECT;
		int morphOpType1 = Imgproc.MORPH_OPEN;
		int morphOpType2 = Imgproc.MORPH_CLOSE;
		int morphOpType3 = Imgproc.MORPH_DILATE;
		
		// Ouverture to get rid of small elements
        Mat element1 = Imgproc.getStructuringElement(elementType1, new Size(8, 8));
        Imgproc.morphologyEx(frameToProcess, frameCl, morphOpType1, element1);
        // Dilatation to make the objects detected bigger.
        Mat element3 = Imgproc.getStructuringElement(elementType1, new Size(20, 20));
        Imgproc.morphologyEx(frameCl, frameCl, morphOpType3, element3);
        // Filling the holes in case of having holes.
        Mat element2 = Imgproc.getStructuringElement(elementType2, new Size(90, 90));
        Imgproc.morphologyEx(frameCl, frameCl, morphOpType2, element2);
        
		return frameCl;
	}

	private void DrawWithContours(Mat srcGray, Mat srcColor, Mat frame) {

		/*Moments moments = Imgproc.moments(srcGray);
		Point centroid = new Point();
		centroid.x = moments.get_m10() / moments.get_m00();
		centroid.y = moments.get_m01() / moments.get_m00();*/
		
		Mat cannyOutput = new Mat();
        Imgproc.Canny(srcGray, cannyOutput, 100, 100 * 2); // 100 is the treshold
        
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        
        Point center = new Point(Double.NaN,Double.NaN);
        
        // Only proceed if at least one contour is detected then choose the largest contour (area).
        if (contours.size() != 0) {
        	MatOfPoint maxContour=contours.get(0);
        	double maxVal = 0;
        	int maxValIdx = 0;
        	for (int i = 0; i < contours.size(); i=i+1) {
        		double contourArea = Imgproc.contourArea(contours.get(i));
        		if (maxVal < contourArea)
        	    {
        	        maxVal = contourArea;
        	        maxValIdx = i;
        	    }
        	}
        	maxContour = contours.get(maxValIdx);
        	
        	
        	// For every found contour we now apply approximation to polygons with accuracy +-3 and 
        	// stating that the curve must be closed. After that we find a bounding rect for every polygon 
        	// and save it to boundRect. At last we find a minimum enclosing circle for every polygon and 
        	// save it to center and radius vectors.
        	// source : https://docs.opencv.org/3.4/da/d0c/tutorial_bounding_rects_circles.html
        	
        	
            MatOfPoint2f contoursPoly  = new MatOfPoint2f();
            Rect boundRect = new Rect();
            float[] radius = new float[1]; // ?

            
            Imgproc.approxPolyDP(new MatOfPoint2f(maxContour.toArray()), contoursPoly, 3, true);
            boundRect = Imgproc.boundingRect(new MatOfPoint(contoursPoly.toArray()));
            Imgproc.minEnclosingCircle(contoursPoly, center, radius);
            
        	
            // Now the drawing       
            List<MatOfPoint> contoursPolyList = new ArrayList<>(1); // il y a qu'un element
            contoursPolyList.add(new MatOfPoint(contoursPoly.toArray()));

            //Imgproc.drawContours(srcGray, contoursPolyList, 0, color);
            //Imgproc.rectangle(srcGray, boundRect.tl(), boundRect.br(), color, 2);
            Imgproc.circle(frame, center, (int) radius[0], new Scalar(0,255,255), 1);
            
            // We add to the number of images detected because this part of code only works if it finds a contour
            if(followRate.isSelected()) {
            	nbrImageDetected +=1;
            	System.out.println("nbr detected : "+nbrImageDetected);
            }
        }

		if (gommebl) { // If we are erasing
			Imgproc.circle(srcColor, center, (int)thickness, new Scalar(0, 0, 0),-1);

			Platform.runLater(() -> {
				label1.setText("Erasing...");
			});

		} else if (drawings.size() != 0) { // If we writing on a sheet

			this.points.add(center);

			if (points.size() > 2) {

				this.points.remove(0);
				
				
				if (Double.isNaN(points.get(1).x) || Double.isNaN(points.get(0).x)) {
					Platform.runLater(() -> {
						label1.setText("Object not detected, no drawing..");
					});

				} else {
					Imgproc.line(srcColor, this.points.get(0), this.points.get(1), color, (int)thickness);
					
					Platform.runLater(() -> {
						label1.setText("Drawing...");
					});
				}
			}

		}

	}

	private ArrayList<Scalar> HSV_RangeDeterminator(Mat HSVframe) {
		// declaring variables
		double h_max = 0, s_max = 0, v_max = 0;
		double h_min = 180; // max for h is 180
		double s_min = 255, v_min = 255;
		Scalar lowThresh = new Scalar(h_min, s_min, v_min);
		Scalar highThresh = new Scalar(h_max, s_max, v_max);

		// getting the low thresh inside the rectangle, i added the +/- 5 in case the
		// user didn't fill the rectangle
		for (int i = 1; i < HSVframe.rows(); i++) {
			for (int j = 1; j < HSVframe.cols(); j++) {
				double[] data = HSVframe.get(i, j); // Stores element in an array HSV has 3 channels

				if (data[0] < h_min)
					h_min = data[0];
				if (data[1] < s_min)
					s_min = data[1];
				if (data[2] < v_min)
					v_min = data[2];
				if (data[0] > h_max)
					h_max = data[0];
				if (data[1] > s_max)
					s_max = data[1];
				if (data[2] > v_max)
					v_max = data[2];
			}
		}
	
		
		lowThresh = new Scalar(h_min, s_min, v_min);
		highThresh = new Scalar(h_max, s_max, v_max);

		ArrayList<Scalar> Thresh = new ArrayList<Scalar>();

		Thresh.add(lowThresh);
		Thresh.add(highThresh);
		
		HminSld.setValue(h_min);
		SminSld.setValue(s_min);
		VminSld.setValue(v_min);
		
		HmaxSld.setValue(h_max);
		SmaxSld.setValue(s_max);
		VmaxSld.setValue(v_max);

		return Thresh;

	}
	
	
	private void checkBrightness(Mat frame)
	{
		// We convert to gray image because it's easier to determine the brightness
		Mat grayFrame = new Mat();
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		// We get the pixel value
		double sum = 0;
		int avg = 0;
		
		// We calculate the average
		for (int x = 0; x < grayFrame.rows(); x++) {
			for (int y = 0; y < grayFrame.cols(); y++) {
				double[] data = grayFrame.get(x, y); //Stores element in an array gray has 1 channel
				sum = sum + data[0];
			}
		}
		avg = (int)Math.floor(sum /(grayFrame.rows()*grayFrame.cols()));
		// System.out.println("the average value is : "+avg);
		
		
		// now we compare with 127 (the average) 80 because 127 didn't work
		if (avg>80) {
			brightness = true;
			
			Platform.runLater(() -> {
				label1.setText("Please select your object by clicking");
			});
		}
		else {
			brightness = false;
			
			Platform.runLater(() -> {
				label1.setText("Please put some light on..");	
			});
		}
	}

	// Creating the mouse event handler clicking for taking points
	EventHandler<MouseEvent> eventHandlerclick = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			double X = e.getX();
			double Y = e.getY();

			Point p = new Point(X, Y);

			if (coord.size() < 2) {
				coord.add(p);
			}

		}

	};
	
	// Creating the mouse event handler pressing for manual drawing
		EventHandler<MouseEvent> eventHandlerpress = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				double X = e.getX();
				double Y = e.getY();

				Point p = new Point(X, Y);
				
				if (modemanuel && coord.size() >= 2 && drawings.size()!=0 && !gommebl) {
					Imgproc.circle(drawings.get(sheetind), p, (int)thickness, color, -1);
				}
				
				if (modemanuel && coord.size() >= 2 && drawings.size()!=0 && gommebl) {
					Imgproc.circle(drawings.get(sheetind), p, (int)thickness, new Scalar(0,0,0), -1);
				}

			}

		};
	
	
	
	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 * 
	 * @param view  the {@link ImageView} to update
	 * @param image the {@link Image} to show
	 */
	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

	/**
	 * On application close, stop the acquisition from the camera
	 */
	protected void setClosed() {
		stopAcquisition();
	}

}