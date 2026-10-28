package RogueLike;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * AssetManager loads game files (images for now, sounds later).
 */
public class AssetManager {

    public static BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println("Could not load image: " + path);
            return null;
        }
    }

    /**
     * NEW: cuts one row of a sprite sheet into individual frames.
     * sheet     = the whole sheet image
     * frameSize = width and height of one frame (64)
     * row       = which row (0 = top row)
     * count     = how many frames are in that row
     */
    public static BufferedImage[] sliceRow(BufferedImage sheet, int frameSize,
                                           int row, int count) {
        BufferedImage[] frames = new BufferedImage[count];
        if (sheet == null) {
            return frames;   // sheet failed to load, so frames stay null (pink box)
        }
        for (int i = 0; i < count; i++) {
            // getSubimage(x, y, width, height) cuts out a rectangle.
            frames[i] = sheet.getSubimage(i * frameSize, row * frameSize,
                                          frameSize, frameSize);
        }
        return frames;
    }
}