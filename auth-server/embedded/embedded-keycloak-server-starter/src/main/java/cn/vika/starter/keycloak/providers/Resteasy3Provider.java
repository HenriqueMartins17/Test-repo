package cn.vika.starter.keycloak.providers;

import com.google.auto.service.AutoService;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.common.util.ResteasyProvider;

@AutoService(ResteasyProvider.class)
public class Resteasy3Provider implements ResteasyProvider {

    @Override
    public <R> R getContextData(Class<R> type) {
        return ResteasyProviderFactory.getContextData(type);
    }

    @Override
    public void pushDefaultContextObject(Class type, Object instance) {
        getContextData(Dispatcher.class).getDefaultContextObjects().put(type, instance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void pushContext(Class type, Object instance) {
        ResteasyProviderFactory.pushContext(type, instance);
    }

    @Override
    public void clearContextData() {
        ResteasyProviderFactory.clearContextData();
    }

}