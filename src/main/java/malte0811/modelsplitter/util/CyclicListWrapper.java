package malte0811.modelsplitter.util;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class CyclicListWrapper<T>
{
	private final List<T> wrapped;

	public CyclicListWrapper(List<T> wrapped)
	{
		this.wrapped = wrapped;
	}

	public T get(int i)
	{
		return wrapped.get(toIndex(i));
	}

	public void set(int i, T value)
	{
		wrapped.set(toIndex(i), value);
	}

	public List<T> sublist(int begin, int end)
	{
		while(begin > end)
		{
			end += wrapped.size();
		}
		ImmutableList.Builder<T> sublist = ImmutableList.builder();
		for(int i = begin; i < end; ++i)
		{
			sublist.add(get(i));
		}
		return sublist.build();
	}

	private int toIndex(int cyclicIndex)
	{
		cyclicIndex = cyclicIndex%wrapped.size();
		return (cyclicIndex+wrapped.size())%wrapped.size();
	}
}
