package edu.eatonlab.imageprofessor.imageproc;
import edu.eatonlab.imageprofessor.communication.Message;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;


public class ImageUtil {

    private static String[] formats = {".jpg", ".gif", ".ppm", ".png", ".bmp"};

    /**
     * Test if the image in certain path is supported by our program
     * @param imagePath
     * @return true if supported, false otherwise
     * TODO: does not handle types of other lengths
     */
    @Deprecated
    private static boolean supportFormat(String imagePath) {
        String formatSpec = imagePath.substring(imagePath.length() - 3, imagePath.length());
        for(int i = 0; i < formats.length; i++) {
            if(formatSpec.equals(formats[i])) return true;
        }
        return false;
    }


    /**
     * Create an image from a image type message
     * @param msg
     * @return
     */
    public static BufferedImage unwrap(Message msg) {
        return (BufferedImage) msg.getObject();
    }

    /**
     * Create a image type message from a image
     * @param image
     * @return
     */
    public static Message wrap(BufferedImage image) {
        return new Message("IMG", Message.TYPE_IMG, "Image", image);
    }


    /**
     * Clear the type of the image
     * @param oldImage
     * @return
     */
    public static BufferedImage detype(Image oldImage) {
        BufferedImage newImage = new BufferedImage(oldImage.getWidth(null),
                oldImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
        newImage.createGraphics().drawImage(oldImage, 0, 0, Color.WHITE, null);
        return newImage;
    }

    /**
     * Store Images
     * @param imageList
     * @throws IOException
     */
    public static void store(LinkedList<BufferedImage> imageList,
                             String keyword, String path) throws IOException {
        int count = 0;

        File indexFile = new File(path + "/index.txt");

        if(!indexFile.exists()) indexFile.createNewFile();
        FileWriter fstream = new FileWriter(path + "/index.txt", true);

        BufferedWriter out = new BufferedWriter(fstream);
        Iterator<BufferedImage> iter = imageList.iterator();

        int cnt = 0;
        while(iter.hasNext()) {

            System.out.println(cnt);
            ImageIO.write(iter.next(), "JPG", new File(path + keyword + (count++) + ".jpg"));
            out.write(keyword + count + ".jpg" + "\n");
            cnt++;
        }
        out.close();
    }

}
