package br.com.fiap.rei_dos_piratas.infrastructure.mapper;

import br.com.fiap.rei_dos_piratas.domain.entity.Page;

public class PageMapper {

    public static <T>Page<T> fromFrameworkPage(org.springframework.data.domain.Page<T> frameworkPage){
        return new Page<T>(
                frameworkPage.getTotalPages(),
                frameworkPage.getNumber(),
                frameworkPage.stream().toList());
    }

    private PageMapper() {
    }
}
