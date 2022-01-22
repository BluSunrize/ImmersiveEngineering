package blusunrize.immersiveengineering.data.icon;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.data.HashCache;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static org.lwjgl.opengl.GL40.*;

public class ModelRenderer
{
    private static final Direction[] DIRECTIONS_AND_NULL = Arrays.copyOf(Direction.values(), 7);
    private static final Random RANDOM = new Random(0);

    private final int width;
    private final int height;
    private final int framebufferID;
    private final int renderedTexture;
    private final int depthBuffer;
    private final File outputDirectory;
    private final HashCache cache;
    private final ItemRenderer itemRenderer;

    public ModelRenderer(int width, int height, final File outputDirectory, HashCache cache)
    {
        this.width = width;
        this.height = height;
        this.outputDirectory = outputDirectory;
        this.cache = cache;
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

        Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack transform = RenderSystem.getModelViewStack();
        transform.pushPose();
        transform.scale(1, -1, 1);
        transform.mulPose(new Quaternion(0, Mth.PI, 0, false));
        RenderSystem.applyModelViewMatrix();
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

        RenderSystem.enableDepthTest();
        transform.popPose();
        RenderSystem.applyModelViewMatrix();

        final TextureCutter cutter = new TextureCutter(256, 256);
        exportTo(filename, () -> RenderSystem.bindTexture(renderedTexture), cutter::cutTexture);
    }

    public void renderQuads(PoseStack matrixStackIn, VertexConsumer bufferIn, List<BakedQuad> quadsIn, ItemStack itemStackIn, int combinedLightIn, int combinedOverlayIn)
    {
        PoseStack.Pose matrixstack$entry = matrixStackIn.last();

        for(BakedQuad bakedquad : quadsIn)
        {
            int i = -1;
            if(bakedquad.isShade())
            {
                i = Minecraft.getInstance().getItemColors().getColor(itemStackIn, bakedquad.getTintIndex());
            }

            float f = (float)(i >> 16&255)/255.0F;
            float f1 = (float)(i >> 8&255)/255.0F;
            float f2 = (float)(i&255)/255.0F;
            bufferIn.putBulkData(matrixstack$entry, bakedquad, f, f1, f2, combinedLightIn, combinedOverlayIn, true);
        }

    }

    private void exportTo(String fileName, Runnable textureBinder, Function<BufferedImage, BufferedImage> imageAdapter)
    {
        textureBinder.run();
        int width = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int height = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
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
            cache.keep(output.toPath());
        } catch(IOException xcp)
        {
            throw new RuntimeException(xcp);
        }
    }
}
