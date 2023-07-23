package pg.gipter.core.dao;

import com.mongodb.MongoClient;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import pg.gipter.statistics.Statistic;

public class StatisticCodec implements CollectibleCodec<Statistic> {

    private final CodecRegistry registry;
    private final Codec<Document> documentCodec;
    private final StatisticConverter converter;

    public StatisticCodec() {
        this.registry = MongoClient.getDefaultCodecRegistry();
        this.documentCodec = this.registry.get(Document.class);
        this.converter = new StatisticConverter();
    }

    public StatisticCodec(Codec<Document> codec) {
        this.documentCodec = codec;
        this.registry = MongoClient.getDefaultCodecRegistry();
        this.converter = new StatisticConverter();
    }

    public StatisticCodec(CodecRegistry registry) {
        this.registry = registry;
        this.documentCodec = this.registry.get(Document.class);
        this.converter = new StatisticConverter();
    }

    @Override
    public void encode(BsonWriter writer, Statistic statistic, EncoderContext encoderContext) {
        Document document = this.converter.convert(statistic);
        documentCodec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<Statistic> getEncoderClass() {
        return Statistic.class;
    }

    @Override
    public Statistic decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);
        return this.converter.convert(document);
    }

    @Override
    public Statistic generateIdIfAbsentFromDocument(Statistic statistic) {
        if (!documentHasId(statistic)) {
            statistic.setId(new ObjectId());
        }

        return statistic;
    }

    @Override
    public boolean documentHasId(Statistic statistic) {
        return (statistic.getId() != null);
    }

    @Override
    public BsonValue getDocumentId(Statistic statistic) {
        if (!documentHasId(statistic)) {
            throw new IllegalStateException("The document does not contain an _id");
        }

        return new BsonString(statistic.getId().toHexString());
    }

}
