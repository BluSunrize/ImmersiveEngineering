package blusunrize.immersiveengineering.data.icon;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.function.Function;

import static org.lwjgl.opengl.GL40.*;

public class ModelRenderer
{
    private final int width;
    private final int height;
    private final int framebufferID;
    private final int renderedTexture;
    private final int depthBuffer;
    private final File outputDirectory;
    private final ItemRenderer itemRenderer;

    public ModelRenderer(int width, int height, final File outputDirectory)
    {
        this.width = width;
        this.height = height;
        this.outputDirectory = outputDirectory;
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.framebufferID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);

        this.renderedTexture = glGenTextures();
        RenderSystem.bindTexture(renderedTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, renderedTexture, 0);

        this.depthBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
        //TODO do we need/want stencil?
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);

        Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // In an ideal world this would be 16 (size of a slot), but some items are slightly bigger
        final float viewSize = 20;
        Matrix4f matrix4f = Matrix4f.orthographic(0, viewSize, 0, viewSize, -200, 3000);
        RenderSystem.setProjectionMatrix(matrix4f);

        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.translate(viewSize/2, viewSize/2, 100);
        modelViewStack.mulPose(new Quaternion(0, 0, Mth.PI, false));
        modelViewStack.scale(1, -1, 1);
        modelViewStack.scale(16, 16, 16);
        RenderSystem.applyModelViewMatrix();
    }

    // TODO free GL resources

    public void renderModel(BakedModel model, String filename, ItemStack stack)
    {
        if(model==null)
            return;
        model = model.getOverrides().resolve(model, stack, null, null, 0);

        // Set up GL
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
        RenderSystem.bindTexture(renderedTexture);
        glViewport(0, 0, width, height);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1);
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

        if(model.usesBlockLight())
            Lighting.setupFor3DItems();
        else
            Lighting.setupForFlatItems();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        MultiBufferSource.BufferSource bufferSources = MultiBufferSource.immediate(builder);

        itemRenderer.render(stack, ItemTransforms.TransformType.GUI, false, new PoseStack(), bufferSources, 15728880, OverlayTexture.NO_OVERLAY, model);

        bufferSources.endBatch();
        if(builder.building())
            builder.end();

        final TextureCutter cutter = new TextureCutter(width, height);
        exportTo(filename, () -> RenderSystem.bindTexture(renderedTexture), cutter::cutTexture);
    }

    private void exportTo(String fileName, Runnable textureBinder, Function<BufferedImage, BufferedImage> imageAdapter)
    {
        textureBinder.run();
        int size = width*height;
        BufferedImage bufferedimage = new BufferedImage(width, height, 2);

        File output = new File(this.outputDirectory, fileName);
        IntBuffer buffer = BufferUtils.createIntBuffer(size);
        int[] data = new int[size];

        glGetTexImage(GL_TEXTURE_2D, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
        buffer.get(data);
        bufferedimage.setRGB(0, 0, width, height, data, 0, width);

        try
        {
            output.getParentFile().mkdirs();
            ImageIO.write(imageAdapter.apply(bufferedimage), "png", output);
        } catch(IOException xcp)
        {
            throw new RuntimeException(xcp);
        }
    }
}
