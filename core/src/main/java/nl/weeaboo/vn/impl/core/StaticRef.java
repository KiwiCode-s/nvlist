package nl.weeaboo.vn.impl.core;

import java.io.Serializable;
import java.util.UUID;

import nl.weeaboo.common.Checks;

public final class StaticRef<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final Class<T> type;

    public StaticRef(String id, Class<T> type) {
        this.id = Checks.checkNotNull(id);
        this.type = Checks.checkNotNull(type);
    }

    public static <T> StaticRef<T> anonymous(Class<T> type) {
        return new StaticRef<>(UUID.randomUUID().toString(), type);
    }
    public static <T> StaticRef<T> from(String id, Class<T> type) {
        return new StaticRef<>(id, type);
    }

    public final T get() {
        return Checks.checkNotNull(getIfPresent(), "StaticRef." + id);
    }

    public final T getIfPresent() {
        StaticEnvironment instance = StaticEnvironment.getInstance();
        return instance.get(this);
    }

    public final void set(T value) {
        StaticEnvironment instance = StaticEnvironment.getInstance();
        instance.set(this, value);
    }

    String getId() {
        return id;
    }

    Class<T> getType() {
        return type;
    }

}