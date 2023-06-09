/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.manual.icon;

import blusunrize.immersiveengineering.client.utils.DummyVertexBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.function.Function;

import static org.lwjgl.opengl.GL40.*;

public class ModelRenderer implements AutoCloseable
{
    private final int width;
    private final int height;
    private final int framebufferID;
    private final int renderedTexture;
    private final int depthBuffer;
    private final File outputDirectory;
    private final ItemRenderer itemRenderer;
    private final RenderBuffers renderBuffers = new RenderBuffers();

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
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);

        glViewport(0, 0, width, height);

        Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // In an ideal world this would be 16 (size of a slot), but some items are slightly bigger
        final float viewSize = 20;
        Matrix4f matrix4f = new Matrix4f().setOrtho(0, viewSize, 0, viewSize, -200, 3000);
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);

        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.translate(viewSize/2, viewSize/2, 100);
        modelViewStack.scale(1, -1, 1);
        modelViewStack.scale(16, 16, 16);
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public void close()
    {
        glDeleteBuffers(framebufferID);
        glDeleteTextures(renderedTexture);
        glDeleteRenderbuffers(depthBuffer);
    }

    public void renderModel(BakedModel model, String filename, ItemStack stack)
    {
        if(model==null)
            return;
        model = model.getOverrides().resolve(model, stack, null, null, 0);

        // Set up GL

        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
        RenderSystem.bindTexture(renderedTexture);

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1);
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);

        if(model.usesBlockLight())
            Lighting.setupFor3DItems();
        else
            Lighting.setupForFlatItems();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        MultiBufferSource.BufferSource bufferSources = renderBuffers.bufferSource();
        // Do not render foil/enchantment glint: This depends on the current time, and we do not want the output to vary
        // randomly. Additionally, glint isn't really visible in the output anyway, I assume this is technically a bug.
        MultiBufferSource noFoilSource = type -> {
            if(type==RenderType.glintDirect())
                return new DummyVertexBuilder(DefaultVertexFormat.BLOCK);
            else
                return bufferSources.getBuffer(type);
        };

        itemRenderer.render(
                stack,
                ItemDisplayContext.GUI, false, new PoseStack(),
                noFoilSource,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                model
        );

        bufferSources.endBatch();
        if(builder.building())
            builder.end();
        renderBuffers.fixedBufferPack().clearAll();

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
