package Imageresources;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageResources {
    public ImageResources() {
    }

    public BufferedImage getTankImage() {
        return getBufferedImage("/images/3.jpg");
    }

    public BufferedImage getTractorImage() {
        return getBufferedImage("/images/4.jpg");
    }

    public BufferedImage getRaceCarRedImage() {
        return getBufferedImage("/images/obi2.jpg");
    }

    public BufferedImage getRaceCarBlueImage() {
        return getBufferedImage("/images/OIP1.jpg");
    }
    public BufferedImage getBakgroundImage(){
        return getBufferedImage("/Images/Background.jpg");
    }

    private BufferedImage getBufferedImage(String name) {
        try {
            return ImageIO.read(this.getClass().getResource(name));
        } catch (IOException var3) {
            IOException e = var3;
            throw new RuntimeException(e);
        }
    }
}