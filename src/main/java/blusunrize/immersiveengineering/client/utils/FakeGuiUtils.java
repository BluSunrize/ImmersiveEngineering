package blusunrize.immersiveengineering.client.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

import static net.minecraftforge.fml.client.gui.GuiUtils.*;

public class FakeGuiUtils
{
	//Copied and modified from Forge's GuiUtils. Move back to using that version once it isn't commented out any more
	public static void drawHoveringText(MatrixStack mStack, List<ITextComponent> inputLines, int mouseX, int mouseY,
										int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font)
	{
		if(!inputLines.isEmpty())
		{
			RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(ItemStack.EMPTY, inputLines, mStack, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth, font);
			if(MinecraftForge.EVENT_BUS.post(event))
				return;
			mouseX = event.getX();
			mouseY = event.getY();
			screenWidth = event.getScreenWidth();
			screenHeight = event.getScreenHeight();
			maxTextWidth = event.getMaxWidth();
			font = event.getFontRenderer();

			RenderSystem.disableRescaleNormal();
			RenderSystem.disableDepthTest();
			int tooltipTextWidth = 0;

			for(ITextComponent textLine : inputLines)
			{
				int textLineWidth = font.func_238414_a_(textLine);
				if(textLineWidth > tooltipTextWidth)
					tooltipTextWidth = textLineWidth;
			}

			boolean needsWrap = false;

			int titleLinesCount = 1;
			int tooltipX = mouseX+12;
			if(tooltipX+tooltipTextWidth+4 > screenWidth)
			{
				tooltipX = mouseX-16-tooltipTextWidth;
				if(tooltipX < 4) // if the tooltip doesn't fit on the screen
				{
					if(mouseX > screenWidth/2)
						tooltipTextWidth = mouseX-12-8;
					else
						tooltipTextWidth = screenWidth-16-mouseX;
					needsWrap = true;
				}
			}

			if(maxTextWidth > 0&&tooltipTextWidth > maxTextWidth)
			{
				tooltipTextWidth = maxTextWidth;
				needsWrap = true;
			}

			List<IReorderingProcessor> textLines;
			if(needsWrap)
			{
				int wrappedTooltipWidth = 0;
				List<IReorderingProcessor> wrappedTextLines = new ArrayList<>();
				for(int i = 0; i < inputLines.size(); i++)
				{
					ITextProperties textLine = inputLines.get(i);
					List<IReorderingProcessor> wrappedLine = font.func_238425_b_(textLine, tooltipTextWidth);
					if(i==0)
						titleLinesCount = wrappedLine.size();

					for(IReorderingProcessor line : wrappedLine)
					{
						int lineWidth = font.func_243245_a(line);
						if(lineWidth > wrappedTooltipWidth)
							wrappedTooltipWidth = lineWidth;
						wrappedTextLines.add(line);
					}
				}
				tooltipTextWidth = wrappedTooltipWidth;
				textLines = wrappedTextLines;

				if(mouseX > screenWidth/2)
					tooltipX = mouseX-16-tooltipTextWidth;
				else
					tooltipX = mouseX+12;
			}
			else
			{
				textLines = new ArrayList<>(inputLines.size());
				for(ITextProperties line : inputLines)
					textLines.add(LanguageMap.getInstance().func_241870_a(line));
			}

			int tooltipY = mouseY-12;
			int tooltipHeight = 8;

			if(inputLines.size() > 1)
			{
				tooltipHeight += (inputLines.size()-1)*10;
				if(inputLines.size() > titleLinesCount)
					tooltipHeight += 2; // gap between title lines and next lines
			}

			if(tooltipY < 4)
				tooltipY = 4;
			else if(tooltipY+tooltipHeight+4 > screenHeight)
				tooltipY = screenHeight-tooltipHeight-4;

			final int zLevel = 400;
			int backgroundColor = DEFAULT_BACKGROUND_COLOR;
			int borderColorStart = DEFAULT_BORDER_COLOR_START;
			int borderColorEnd = DEFAULT_BORDER_COLOR_END;
			RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(ItemStack.EMPTY, inputLines, mStack, tooltipX, tooltipY, font, backgroundColor, borderColorStart, borderColorEnd);
			MinecraftForge.EVENT_BUS.post(colorEvent);
			backgroundColor = colorEvent.getBackground();
			borderColorStart = colorEvent.getBorderStart();
			borderColorEnd = colorEvent.getBorderEnd();

			mStack.push();
			Matrix4f mat = mStack.getLast().getMatrix();
			//TODO, lots of unnessesary GL calls here, we can buffer all these together.
			drawGradientRect(mat, zLevel, tooltipX-3, tooltipY-4, tooltipX+tooltipTextWidth+3, tooltipY-3, backgroundColor, backgroundColor);
			drawGradientRect(mat, zLevel, tooltipX-3, tooltipY+tooltipHeight+3, tooltipX+tooltipTextWidth+3, tooltipY+tooltipHeight+4, backgroundColor, backgroundColor);
			drawGradientRect(mat, zLevel, tooltipX-3, tooltipY-3, tooltipX+tooltipTextWidth+3, tooltipY+tooltipHeight+3, backgroundColor, backgroundColor);
			drawGradientRect(mat, zLevel, tooltipX-4, tooltipY-3, tooltipX-3, tooltipY+tooltipHeight+3, backgroundColor, backgroundColor);
			drawGradientRect(mat, zLevel, tooltipX+tooltipTextWidth+3, tooltipY-3, tooltipX+tooltipTextWidth+4, tooltipY+tooltipHeight+3, backgroundColor, backgroundColor);
			drawGradientRect(mat, zLevel, tooltipX-3, tooltipY-3+1, tooltipX-3+1, tooltipY+tooltipHeight+3-1, borderColorStart, borderColorEnd);
			drawGradientRect(mat, zLevel, tooltipX+tooltipTextWidth+2, tooltipY-3+1, tooltipX+tooltipTextWidth+3, tooltipY+tooltipHeight+3-1, borderColorStart, borderColorEnd);
			drawGradientRect(mat, zLevel, tooltipX-3, tooltipY-3, tooltipX+tooltipTextWidth+3, tooltipY-3+1, borderColorStart, borderColorStart);
			drawGradientRect(mat, zLevel, tooltipX-3, tooltipY+tooltipHeight+2, tooltipX+tooltipTextWidth+3, tooltipY+tooltipHeight+3, borderColorEnd, borderColorEnd);

			MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(ItemStack.EMPTY, inputLines, mStack, tooltipX, tooltipY, font, tooltipTextWidth, tooltipHeight));

			IRenderTypeBuffer.Impl renderType = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
			mStack.translate(0.0D, 0.0D, zLevel);

			int tooltipTop = tooltipY;

			for(int lineNumber = 0; lineNumber < inputLines.size(); ++lineNumber)
			{
				IReorderingProcessor line = textLines.get(lineNumber);
				if(line!=null)
					font.func_238416_a_(line, (float)tooltipX, (float)tooltipY, -1, true, mat, renderType, false, 0, 15728880);

				if(lineNumber+1==titleLinesCount)
					tooltipY += 2;

				tooltipY += 10;
			}

			renderType.finish();
			mStack.pop();

			MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(ItemStack.EMPTY, inputLines, mStack, tooltipX, tooltipTop, font, tooltipTextWidth, tooltipHeight));

			RenderSystem.enableDepthTest();
			RenderSystem.enableRescaleNormal();
		}
	}
}
