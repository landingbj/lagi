package ai.lagi.utils;

import java.util.Iterator;
import java.util.function.Function;

public class MappingIterable<A, B> implements Iterable<B> {
    private final Iterable<A> source;
    private final Function<A, B> mapper;

    public MappingIterable(Iterable<A> source, Function<A, B> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    public Iterator<B> iterator() {
        return new Iterator<B>() {
            private final Iterator<A> sourceIterator = source.iterator();

            @Override
            public boolean hasNext() {
                return sourceIterator.hasNext();
            }

            @Override
            public B next() {
                return mapper.apply(sourceIterator.next());
            }
        };
    }
}