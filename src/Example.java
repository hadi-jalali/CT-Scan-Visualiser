//Mohammad Hadi Jalali Lak (932173) and all of this code is my individual work

/**
 * File name:Example.java
 * @author Mohammad Hadi Jalali lak , Mark Jones
 * Copyright: no copyright
 * Created: 10.2.20
 * @since 1.3.20
 * @program_purpose:get some raw Cthead data from a file and output
 *  images with the ability to resize and histogram equalization
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;

// OK this is not best practice - maybe you'd like to create
// a volume data class?
// I won't give extra marks for that though.

public class Example extends Application {
    short cthead[][][]; //store the 3D volume data set
    short min, max; //min/max value in the 3D volume data set
    int zValue;
    int yValue;
    int xValue;
    int size = 256;
    float wRatio;
    float hRatio;
    private boolean showHistogram = false;

    @Override
    public void start(Stage stage) throws FileNotFoundException, IOException {
        stage.setTitle("CThead Viewer");


        ReadData();


        int width = 256;
        int height = 256;
        int depth = 113;
        //creating image and image view instances for all 6 different images
        WritableImage medical_imageTop = new WritableImage(width, height);
        WritableImage medical_imageFront = new WritableImage(width, depth);
        WritableImage medical_imageSide = new WritableImage(width, depth);
        WritableImage histogramTopImage = new WritableImage(width, height);
        WritableImage histogramSideImage = new WritableImage(width, depth);
        WritableImage histogramFrontImage = new WritableImage(width, depth);
        ImageView imageViewTop = new ImageView(medical_imageTop);
        ImageView imageViewFront = new ImageView(medical_imageFront);
        ImageView imageViewSide = new ImageView(medical_imageSide);
        ImageView histogramTopView = new ImageView(histogramTopImage);
        ImageView histogramSideView = new ImageView(histogramSideImage);
        ImageView histogramFrontView = new ImageView(histogramFrontImage);


        Button mip_button = new Button("MIP");
        Button resize_button = new Button("Resize!");
        Button histogram_button = new Button("Histogram equalization");
        //sliders to step through the slices (z and y directions) (remember 113 slices in z direction 0-112)
        Slider zslider = new Slider(0, 113, 0);
        zslider.setShowTickLabels(true);
        zslider.setShowTickMarks(true);
        Slider yslider = new Slider(0, 255, 0);
        yslider.setShowTickLabels(true);
        yslider.setShowTickMarks(true);
        Slider xslider = new Slider(0, 255, 0);
        xslider.setShowTickLabels(true);
        xslider.setShowTickMarks(true);
        Slider sizeslider = new Slider(50, 512, 256);
        sizeslider.setShowTickLabels(true);
        sizeslider.setShowTickMarks(true);
        Slider histogramSlider = new Slider(0, 113, 0);
        histogramSlider.setShowTickMarks(true);
        histogramSlider.setShowTickLabels(true);

        mip_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (imageViewTop.getBoundsInLocal().getWidth() == 256) {
                    imageViewTop.setImage(mipTop(medical_imageTop));
                    imageViewSide.setImage(mipSide(medical_imageSide));
                    imageViewFront.setImage(mipFront(medical_imageFront));
                } else {
                    imageViewTop.setImage(resizeImage(mipTop(medical_imageTop)));
                    imageViewSide.setImage(resizeImage(mipSide(medical_imageSide)));
                    imageViewFront.setImage(resizeImage(mipFront(medical_imageFront)));

                }
            }
        });

        histogram_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //check if the image is in the original size
                if (imageViewTop.getBoundsInLocal().getWidth() == 256) {
                    histogramTopView.setImage(histogramTopEqualization(histogramTopImage));
                    histogramSideView.setImage(histogramSideEqualization(histogramSideImage));
                    histogramFrontView.setImage(histogramFrontEqualization(histogramFrontImage));
                } else {
                    histogramTopView.setImage(resizeImage(histogramTopEqualization(histogramTopImage)));
                    histogramSideView.setImage(resizeImage(histogramSideEqualization(histogramSideImage)));
                    histogramFrontView.setImage(resizeImage(histogramFrontEqualization(histogramFrontImage)));
                }
                showHistogram = true;

            }
        });

        resize_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                imageViewSide.setImage(resizeImage(medical_imageSide));
                imageViewFront.setImage(resizeImage(medical_imageFront));
                imageViewTop.setImage(resizeImage(medical_imageTop));
                //check if there are any histogram images
                if (showHistogram == true) {
                    histogramTopView.setImage(resizeImage(histogramTopImage));
                    histogramSideView.setImage(resizeImage(histogramSideImage));
                    histogramFrontView.setImage(resizeImage(histogramFrontImage));
                }
            }
        });

        sizeslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        size = newValue.intValue();
                    }
                });

        zslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        zValue = newValue.intValue();
                        imageViewTop.setImage(resizeImage(getTopView(medical_imageTop)));
                        if (showHistogram == true) {
                            histogramTopView.setImage(resizeImage(histogramTopEqualization(histogramTopImage)));
                        }

                        System.out.println(newValue.intValue());
                    }
                });

        yslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        yValue = newValue.intValue();
                        if (imageViewFront.getBoundsInLocal().getWidth() == 256) {
                            imageViewFront.setImage(getFrontView(medical_imageFront));
                            if (showHistogram == true) {
                                histogramFrontView.setImage(histogramFrontEqualization(histogramFrontImage));
                            }
                        } else {
                            imageViewFront.setImage(resizeImage(getFrontView(medical_imageFront)));
                            if (showHistogram == true) {
                                histogramFrontView.setImage(resizeImage(histogramFrontEqualization(histogramFrontImage)));
                            }
                        }
                        System.out.println(newValue.intValue());
                    }
                });

        xslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number>
                                                observable, Number oldValue, Number newValue) {
                        xValue = newValue.intValue();
                        if (imageViewSide.getBoundsInLocal().getWidth() == 256) {
                            imageViewSide.setImage(getSideView(medical_imageSide));
                            if (showHistogram == true) {
                                histogramSideView.setImage(histogramSideEqualization(histogramSideImage));
                            }
                        } else {
                            imageViewSide.setImage(resizeImage(getSideView(medical_imageSide)));
                            if (showHistogram == true) {
                                histogramSideView.setImage(resizeImage(histogramSideEqualization(histogramSideImage)));
                            }
                        }
                        System.out.println(newValue.intValue());
                    }
                });

        FlowPane root = new FlowPane();
        root.setVgap(5);
        root.setHgap(4);
        VBox sideViewBox = new VBox();
        sideViewBox.getChildren().addAll(imageViewSide, xslider, histogramSideView);
        VBox topViewBox = new VBox();
        topViewBox.getChildren().addAll(imageViewTop, zslider, histogramTopView);
        VBox frontViewBox = new VBox();
        frontViewBox.getChildren().addAll(imageViewFront, yslider, histogramFrontView);
        HBox buttonBox = new HBox();
        buttonBox.getChildren().addAll(resize_button, mip_button, histogram_button);
        VBox box4 = new VBox();
        box4.getChildren().addAll(sizeslider, buttonBox);
        HBox box5 = new HBox();
        box5.getChildren().addAll(sideViewBox, frontViewBox, topViewBox, box4);
        box5.setSpacing(5);


//https://examples.javacodegeeks.com/desktop-java/javafx/scene/image-scene/javafx-image-example/

        root.getChildren().addAll(box5);

        Scene scene = new Scene(root, 1200, 600);
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    public WritableImage histogramTopEqualization(WritableImage image) {
        min = -1117;
        max = 2248;
        int size = 256 * 256 * 113;
        int index;
        float[] mapping = new float[3366];
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        short datum;
        float col;
        int t_i = 0;
        PixelWriter image_writer = image.getPixelWriter();
        int[] histogram = new int[max - min + 1];
        for (k = 0; k < 113; k++) {
            for (j = 0; j < 256; j++) {
                for (i = 0; i < 256; i++) {
                    index = cthead[k][j][i] - min;
                    histogram[index]++;
                }

            }
        }
        for (i = 0; i < max - min + 1; i++) {
            t_i = histogram[i] + t_i;
            mapping[i] = (float) t_i / (float) size;
        }
        for (j = 0; j < h; j++) {
            for (i = 0; i < w; i++) {
                datum = cthead[zValue][j][i];
                col = mapping[datum - min];
                image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

            }
        }
        return image;
    }

    public WritableImage histogramFrontEqualization(WritableImage image) {
        min = -1117;
        max = 2248;
        int size = 256 * 256 * 113;
        int index;
        float[] mapping = new float[3366];
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        short datum;
        float col;
        int t_i = 0;
        PixelWriter image_writer = image.getPixelWriter();
        int[] histogram = new int[max - min + 1];
        for (k = 0; k < 113; k++) {
            for (j = 0; j < 256; j++) {
                for (i = 0; i < 256; i++) {
                    index = cthead[k][j][i] - min;
                    histogram[index]++;
                }

            }
        }
        for (i = 0; i < max - min + 1; i++) {
            t_i = +histogram[i] + t_i;
            mapping[i] = ((float) t_i / (float) size);
        }
        for (k = 0; k < 113; k++) {
            for (i = 0; i < w; i++) {
                datum = cthead[k][yValue][i];
                col = mapping[datum - min];
                image_writer.setColor(i, k, Color.color(col, col, col, 1.0));

            }
        }
        return image;
    }

    public WritableImage histogramSideEqualization(WritableImage image) {
        min = -1117;
        max = 2248;
        int size = 256 * 256 * 113;
        int index;
        float[] mapping = new float[3366];
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        short datum;
        float col;
        int t_i = 0;
        PixelWriter image_writer = image.getPixelWriter();
        int[] histogram = new int[max - min + 1];
        for (k = 0; k < 113; k++) {
            for (j = 0; j < 256; j++) {
                for (i = 0; i < 256; i++) {
                    index = cthead[k][j][i] - min;
                    histogram[index]++;
                }

            }
        }
        for (i = 0; i < max - min + 1; i++) {
            t_i = +histogram[i] + t_i;
            mapping[i] = ((float) t_i / (float) size);
        }
        for (k = 0; k < 113; k++) {
            for (j = 0; j < 256; j++) {
                datum = cthead[k][j][xValue];
                col = mapping[datum - min];
                image_writer.setColor(j, k, Color.color(col, col, col, 1.0));

            }
        }
        return image;
    }

    public WritableImage resizeImage(WritableImage oldImage) {
        int w = (int) oldImage.getWidth(), h = (int) oldImage.getHeight(), i, j, c;
        Color col;
        int h2 = size;
        int w2 = size;
        hRatio = (float) h2 / (float) h;
        wRatio = (float) w2 / (float) w;
        WritableImage newImage = new WritableImage(w2, h2);
        PixelReader image_reader = oldImage.getPixelReader();
        PixelWriter image_writer = newImage.getPixelWriter();
        for (j = 0; j < h2; j++) {
            for (i = 0; i < w2; i++) {
                for (c = 0; c < 3; c++) {
                    //commented out NN
                    //float x1=i/ratio;
                    //float y1=j/ratio;
                    //Color col = image_reader.getColor((int) Math.floor(x1), (int) Math.floor(y1));
                    col = bilinearInterpolation(i, j, oldImage);
                    image_writer.setColor(i, j, col);
                }

            }
        }
        return newImage;

    }

    public Color bilinearInterpolation(int i, int j, WritableImage oldImage) {
        PixelReader image_reader = oldImage.getPixelReader();
        int x1 = (int) (Math.floor(i / wRatio));
        int x2 = x1 + 1;
        int y1 = (int) (Math.floor(j / hRatio));
        int y2 = y1 + 1;
        if (x1 >= oldImage.getWidth() - 1) x1 = x2 = (int) oldImage.getWidth() - 1;
        if (y1 >= oldImage.getHeight() - 1) y1 = y2 = (int) (oldImage.getHeight() - 1);
        Color col11 = image_reader.getColor(x1, y1);
        double red11 = col11.getRed();
        Color col21 = image_reader.getColor(x2, y1);
        double red21 = col21.getRed();
        Color col12 = image_reader.getColor(x1, y2);
        double red12 = col12.getRed();
        Color col22 = image_reader.getColor(x2, y2);
        double red22 = col22.getRed();
        double colx1 = red11 + ((red21 - red11) * ((i - (x1 * wRatio)) / wRatio));
        double colx2 = red12 + ((red22 - red12) * ((i - (x1 * wRatio)) / wRatio));
        double col = colx1 + ((colx2 - colx1) * ((j - (y1 * hRatio)) / hRatio));
        return Color.color(col, col, col, 1.0);


    }

    //Function to read in the cthead data set
    public void ReadData() throws IOException {
        //File name is hardcoded here - much nicer to have a dialog to select it and capture the size from the user
        File file = new File("CThead");
        //Read the data quickly via a buffer (in C++ you can just do a single fread - I couldn't find if there is an equivalent in Java)
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        int i, j, k; //loop through the 3D data set

        min = Short.MAX_VALUE;
        max = Short.MIN_VALUE; //set to extreme values
        short read; //value read in
        int b1, b2; //data is wrong Endian (check wikipedia) for Java so we need to swap the bytes around

        cthead = new short[113][256][256]; //allocate the memory - note this is fixed for this data set
        //loop through the data reading it in
        for (k = 0; k < 113; k++) {
            for (j = 0; j < 256; j++) {
                for (i = 0; i < 256; i++) {
                    //because the Endianess is wrong, it needs to be read byte at a time and swapped
                    b1 = ((int) in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    b2 = ((int) in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    read = (short) ((b2 << 8) | b1); //and swizzle the bytes around
                    if (read < min) min = read; //update the minimum
                    if (read > max) max = read; //update the maximum
                    cthead[k][j][i] = read; //put the short into memory (in C++ you can replace all this code with one fread)
                }
            }
        }
        System.out.println(min + " " + max); //diagnostic - for CThead this should be -1117, 2248
        //(i.e. there are 3366 levels of grey (we are trying to display on 256 levels of grey)
        //therefore histogram equalization would be a good thing
    }


    /*
       This function shows how to carry out an operation on an image.
       It obtains the dimensions of the image, and then loops through
       the image carrying out the copying of a slice of data into the
       image.
   */
    public WritableImage getTopView(WritableImage image) {
        //Get image dimensions, and declare loop variables
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        PixelWriter image_writer = image.getPixelWriter();

        float col;
        short datum;
        //Shows how to loop through each pixel and colour
        //Try to always use j for loops in y, and i for loops in x
        //as this makes the code more readable
        for (j = 0; j < h; j++) {
            for (i = 0; i < w; i++) {
                //at this point (i,j) is a single pixel in the image
                //here you would need to do something to (i,j) if the image size
                //does not match the slice size (e.g. during an image resizing operation
                //If you don't do this, your j,i could be outside the array bounds
                //In the framework, the image is 256x256 and the data set slices are 256x256
                //so I don't do anything - this also leaves you something to do for the assignment
                datum = cthead[zValue][j][i]; //get values from slice 76 (change this in your assignment)
                //calculate the colour by performing a mapping from [min,max] -> [0,255]
                col = (((float) datum - (float) min) / ((float) (max - min)));
                for (c = 0; c < 3; c++) {
                    //and now we are looping through the bgr components of the pixel
                    //set the colour component c of pixel (i,j)
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                    //					data[c+3*i+3*j*w]=(byte) col;
                } // colour loop
            } // column loop
        }
        return image;// row loop
    }

    public WritableImage getFrontView(WritableImage image) {
        //Get image dimensions, and declare loop variables
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        PixelWriter image_writer = image.getPixelWriter();

        float col;
        short datum;
        for (k = 0; k < 113; k++) {
            for (i = 0; i < w; i++) {
                datum = cthead[k][yValue][i];
                col = (((float) datum - (float) min) / ((float) (max - min)));
                for (c = 0; c < 3; c++) {
                    image_writer.setColor(i, k, Color.color(col, col, col, 1.0));
                } // colour loop
            } // column loop
        }
        return image;// row loop
    }

    public WritableImage getSideView(WritableImage image) {
        //Get image dimensions, and declare loop variables
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        PixelWriter image_writer = image.getPixelWriter();

        float col;
        short datum;
        for (k = 0; k < 113; k++) {
            for (j = 0; j < 256; j++) {
                datum = cthead[k][j][xValue];
                col = (((float) datum - (float) min) / ((float) (max - min)));
                for (c = 0; c < 3; c++) {
                    image_writer.setColor(j, k, Color.color(col, col, col, 1.0));
                } // colour loop
            } // column loop
        }
        return image;// row loop
    }

    public WritableImage mipSide(WritableImage image) {
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        float col;
        short datum;
        PixelWriter image_writer = image.getPixelWriter();
        for (k = 0; k < h; k++) {
            for (j= 0; j < w; j++) {
                short maximum = Short.MIN_VALUE;
                for (i = 0; i < 256; i++) {
                    if (cthead[k][j][i] > maximum) {
                        maximum = cthead[k][j][i];
                    }
                }
                datum = maximum;
                col = (((float) datum - (float) min) / ((float) (max - min)));
                for (c = 0; c < 3; c++) {
                    image_writer.setColor(j, k, Color.color(col, col, col, 1.0));
                }
            }
        }
        return image;
    }

    public WritableImage mipFront(WritableImage image) {
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        float col;
        short datum;
        PixelWriter image_writer = image.getPixelWriter();
        for (k = 0; k < h; k++) {
            for (i = 0; i < w; i++) {
                short maximum = Short.MIN_VALUE;
                for (j = 0; j < 256; j++) {
                    if (cthead[k][j][i] > maximum) {
                        maximum = cthead[k][j][i];
                    }
                }
                datum = maximum;
                col = (((float) datum - (float) min) / ((float) (max - min)));
                for (c = 0; c < 3; c++) {
                    image_writer.setColor(i, k, Color.color(col, col, col, 1.0));
                }
            }
        }
        return image;
    }

    public WritableImage mipTop(WritableImage image) {
        int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
        float col;
        short datum;
        PixelWriter image_writer = image.getPixelWriter();
        for (j = 0; j < h; j++) {
            for (i = 0; i < w; i++) {
                short maximum = Short.MIN_VALUE;
                for (k = 0; k < 113; k++) {
                    if (cthead[k][j][i] > maximum) {
                        maximum = cthead[k][j][i];
                    }
                }
                datum = (short) maximum;
                col = (((float) datum - (float) min) / ((float) (max - min)));
                for (c = 0; c < 3; c++) {
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                }
            }
        }
        return image;
    }

    public static void main(String[] args) {
        launch();
    }

}