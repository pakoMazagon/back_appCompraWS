package com.paco.app.controller;

import com.paco.app.entity.Producto;
import com.paco.app.repository.ProductoRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ProductoController(ProductoRepository productoRepository, SimpMessagingTemplate messagingTemplate) {
        this.productoRepository = productoRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/productos")
    @MessageMapping("/productos")
    @SendTo("/topic/lista-compra")
    public List<Producto> obtenerProductos() {
        return productoRepository.findAll();
    }

    @MessageMapping("/addProductos") // Este coincide con "/app/productos" en el cliente
    @SendTo("/topic/lista-compra")   // Envía la lista actualizada a los clientes suscritos
    public List<Producto> agregarProducto(@RequestBody Producto producto) {
        Producto nuevoProducto = productoRepository.save(producto);
        messagingTemplate.convertAndSend("/topic/lista-compra", productoRepository.findAll());
        return productoRepository.findAll();
    }

    @MessageMapping("/removeProductos")
    @SendTo("/topic/lista-compra")
    public List<Producto> removeProducto(@RequestBody Producto producto) {
        productoRepository.deleteById(producto.getId());
        messagingTemplate.convertAndSend("/topic/lista-compra", productoRepository.findAll());
        return productoRepository.findAll();
    }

    @MessageMapping("/updateProducto") 
    @SendTo("/topic/lista-compra")   
    public void updateProducto(@RequestBody Producto producto) {
        Producto productBBDD = productoRepository.findById(producto.getId()).orElseThrow();
        this.productoRepository.save(producto);
        messagingTemplate.convertAndSend("/topic/lista-compra", productoRepository.findAll());
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }
}
