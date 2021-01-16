package blusunrize.immersiveengineering.common.data;

import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RunCompleteHelper implements IDataProvider
{
	@Override
	public void act(DirectoryCache cache) throws IOException
	{
		Path toCreate = Paths.get("ie_data_gen_done");
		if(!Files.exists(toCreate))
			Files.createFile(toCreate);
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "Create a file to allow crashes in datagen to be detected";
	}
}
