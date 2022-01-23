package blusunrize.immersiveengineering.data.icon;

import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.wires.IEWireTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;

public class RenderedItemModelDataProvider implements DataProvider
{

    private final DataGenerator generator;
    private final ExistingFileHelper helper;

    public RenderedItemModelDataProvider(final DataGenerator generator, ExistingFileHelper helper)
    {
        this.generator = generator;
        this.helper = helper;
    }

    @Override
    public void run(HashCache pCache) throws IOException
    {
        GameInitializationManager.getInstance().initialize(helper, generator);
        IEWireTypes.setup();

        final Path itemOutputDirectory = this.generator.getOutputFolder().getParent().resolve("web/icons/item");
        ModelRenderer itemRenderer = new ModelRenderer(256, 256, itemOutputDirectory.toFile());

        Field item_renderProperties;
        try
        {
            item_renderProperties = Item.class.getDeclaredField("renderProperties");
            item_renderProperties.setAccessible(true);
        } catch(NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
        IEItems.REGISTER.getEntries().forEach(regEntry -> {
            var item = regEntry.get();
            item.initializeClient(properties -> {
                try
                {
                    item_renderProperties.set(item, properties);
                } catch(IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
            });
            var name = regEntry.getId();
            ModelResourceLocation modelLocation = new ModelResourceLocation(name, "inventory");

            final BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
            itemRenderer.renderModel(model, name.getNamespace()+"/"+name.getPath()+".png", new ItemStack(item));
        });
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "Item Renderer";
    }
}
