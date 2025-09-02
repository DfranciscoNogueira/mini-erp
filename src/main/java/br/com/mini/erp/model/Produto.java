package br.com.mini.erp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_produtos_sku", columnNames = "sku")
        },
        indexes = {
                @Index(name = "ix_produtos_nome", columnList = "nome"),
                @Index(name = "ix_produtos_ativo", columnList = "ativo")
        })
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal precoBruto;

    @Column(nullable = false)
    private Integer estoque;

    @Column(nullable = false)
    private Integer estoqueMinimo;

    @Column(nullable = false)
    private Boolean ativo = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPrecoBruto() {
        return precoBruto;
    }

    public void setPrecoBruto(BigDecimal precoBruto) {
        this.precoBruto = precoBruto;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(Integer estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

}
