package com.icthh.xm.ms.entity.repository.search.elasticsearch;

import static java.util.Optional.of;

import com.icthh.xm.ms.entity.config.IndexConfiguration;
import com.icthh.xm.ms.entity.config.MappingConfiguration;
import com.icthh.xm.ms.entity.domain.XmEntity;
import com.icthh.xm.ms.entity.repository.search.XmEntitySearchRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
@ConditionalOnProperty(value = "application.use-elasticsearch-multiple-indices",
    havingValue = "false", matchIfMissing = true)
@Slf4j
public class XmEntitySingleIndexElasticRepository implements XmEntityElasticRepository {

    private final MappingConfiguration mappingConfiguration;
    private final IndexConfiguration indexConfiguration;
    private final XmEntitySearchRepository xmEntitySearchRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public XmEntity save(XmEntity xmEntity) {
        return xmEntitySearchRepository.save(xmEntity);
    }

    @Override
    public void saveAll(List<XmEntity> xmEntity) {
        xmEntitySearchRepository.saveAll(xmEntity);
    }

    @Override
    public void delete(XmEntity xmEntity) {
        xmEntitySearchRepository.delete(xmEntity);
    }

    @Override
    public long handleReindex(Function<Specification, Long> specificationFunction) {
        recreateIndex();
        return specificationFunction.apply(null);
    }

    private void recreateIndex() {

        final Class<XmEntity> clazz = XmEntity.class;
        StopWatch stopWatch = StopWatch.createStarted();
        elasticsearchTemplate.deleteIndex(clazz);
        try {

            of(indexConfiguration.isConfigExists())
                .filter(Boolean::valueOf)
                .ifPresentOrElse(
                    isConfigExists -> elasticsearchTemplate.createIndex(clazz, indexConfiguration.getConfiguration()),
                    () -> elasticsearchTemplate.createIndex(clazz));
        } catch (ResourceAlreadyExistsException e) {
            log.info("Do nothing. Index was already concurrently recreated by some other service");
        }

        of(mappingConfiguration.isMappingExists())
            .filter(Boolean::valueOf)
            .ifPresentOrElse(
                isMappingExists -> elasticsearchTemplate.putMapping(clazz, mappingConfiguration.getMapping()),
                () -> elasticsearchTemplate.putMapping(clazz));
        log.info("elasticsearch index was recreated for {} in {} ms",
            XmEntity.class.getSimpleName(), stopWatch.getTime());
    }


}
