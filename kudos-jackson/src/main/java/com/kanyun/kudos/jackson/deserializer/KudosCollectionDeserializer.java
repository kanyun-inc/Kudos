package com.kanyun.kudos.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Benny Huo on 2023/9/6
 */
public class KudosCollectionDeserializer extends CollectionDeserializer {

    private final Class<?> originalCollectionType;

    protected KudosCollectionDeserializer(CollectionDeserializer src, Class<?> originalCollectionType) {
        super(src);
        this.originalCollectionType = originalCollectionType;
    }

    @Override
    protected CollectionDeserializer withResolved(JsonDeserializer<?> dd, JsonDeserializer<?> vd, TypeDeserializer vtd, NullValueProvider nuller, Boolean unwrapSingle) {
        return new KudosCollectionDeserializer(super.withResolved(dd, vd, vtd, nuller, unwrapSingle), originalCollectionType);
    }

    @Override
    protected Collection<Object> _deserializeFromArray(JsonParser p, DeserializationContext ctxt, Collection<Object> result) throws IOException {
        // [databind#631]: Assign current value, to be accessible by custom serializers
        p.setCurrentValue(result);

        JsonDeserializer<Object> valueDes = _valueDeserializer;
        // Let's offline handling of values with Object Ids (simplifies code here)
        if (valueDes.getObjectIdReader() != null) {
            return _deserializeWithObjectId(p, ctxt, result);
        }
        final TypeDeserializer typeDeser = _valueTypeDeserializer;
        JsonToken t;
        while ((t = p.nextToken()) != JsonToken.END_ARRAY) {
            try {
                Object value;
                if (t == JsonToken.VALUE_NULL) {
                    if (_skipNullValues) {
                        continue;
                    }
                    value = _nullProvider.getNullValue(ctxt);
                } else if (typeDeser == null) {
                    value = valueDes.deserialize(p, ctxt);
                } else {
                    value = valueDes.deserializeWithType(p, ctxt, typeDeser);
                }
                if (value == null) {
                    throw new NullPointerException("Element cannot be null for " + originalCollectionType.getName() + ".");
                }
                result.add(value);

                /* 17-Dec-2017, tatu: should not occur at this level...
            } catch (UnresolvedForwardReference reference) {
                throw JsonMappingException
                    .from(p, "Unresolved forward reference but no identity info", reference);
                */
            } catch (Exception e) {
                boolean wrap = (ctxt == null) || ctxt.isEnabled(DeserializationFeature.WRAP_EXCEPTIONS);
                if (!wrap) {
                    ClassUtil.throwIfRTE(e);
                }
                throw JsonMappingException.wrapWithPath(e, result, result.size());
            }
        }
        return result;
    }
}
