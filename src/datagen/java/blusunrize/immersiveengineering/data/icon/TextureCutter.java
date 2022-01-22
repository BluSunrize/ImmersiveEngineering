package blusunrize.immersiveengineering.data.icon;

import java.awt.image.BufferedImage;

public class TextureCutter
{
	private final int width;
	private final int height;

	public TextureCutter(final int width, final int height)
	{
		this.width = width;
		this.height = height;
	}

	public BufferedImage cutTexture(BufferedImage source)
	{
		BufferedImage bufferedimage = new BufferedImage(width, height, 2);

		int minX = source.getWidth()+1;
		int minY = source.getWidth()+1;
		int maxX = -1;
		int maxY = -1; //We assume a square image.
		for(int x = 0; x < source.getWidth(); x++)
		{
			for(int y = 0; y < source.getHeight(); y++)
			{
				final int argb = source.getRGB(x, y);
				final int alpha = (argb >> 24)&0xff;
				if(alpha > 0)
				{
					if(x < minX)
						minX = x;
					if(y < minY)
						minY = y;
					if(x > maxX)
						maxX = x;
					if(y > maxY)
						maxY = y;
				}
			}
		}

		if(minX==source.getWidth()+1||
				minY==source.getWidth()+1||
				maxX==-1||
				maxY==-1||
				minX >= maxX||
				minY >= maxY)
			return bufferedimage; //empty image.

		final int newWidth = maxX-minX;
		final int newHeight = maxY-minY;
		if(newWidth > width)
			return bufferedimage;

		if(newHeight > height)
			return bufferedimage;

		final int xOffset = (width-newWidth)/2;
		final int yOffset = (height-newHeight)/2;

		for(int x = 0; x <= newWidth; x++)
		{
			for(int y = 0; y < newHeight; y++)
			{
				bufferedimage.setRGB(
						xOffset+x,
						yOffset+y,
						source.getRGB(
								minX+x,
								minY+y
						)
				);
			}
		}

		return bufferedimage;
	}
}
