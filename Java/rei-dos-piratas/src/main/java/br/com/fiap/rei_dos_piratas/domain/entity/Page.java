package br.com.fiap.rei_dos_piratas.domain.entity;

import java.util.List;

public record Page<T>(int numberOfPages,
                      int pageNumber,
                      List<T> pageItems)
{}
