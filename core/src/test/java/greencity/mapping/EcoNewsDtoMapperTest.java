package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.econews.EcoNewsDto;
import greencity.entity.EcoNews;
import greencity.entity.Tag;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class EcoNewsDtoMapperTest {
    @InjectMocks
    EcoNewsDtoMapper ecoNewsDtoMapper;

    @Test
    public void convertTest() {
        EcoNews ecoNews = ModelUtils.getEcoNews();

        EcoNewsDto expected = new EcoNewsDto(ecoNews.getCreationDate(), ecoNews.getImagePath(),
            ecoNews.getId(), ecoNews.getTitle(), ecoNews.getText(),
            ModelUtils.getEcoNewsAuthorDto(),
            ecoNews.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList())
        );

        assertEquals(expected, ecoNewsDtoMapper.convert(ecoNews));
    }
}
