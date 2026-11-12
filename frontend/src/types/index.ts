export interface Cliente {
  idCliente: string;
  nombreCompleto: string;
  ciudad: string;
  correo: string;
  telefono: string;
  categoria?: string | null;
  fechaRegistro: string;
  actualizadoEn: string;
}

export interface CrearClientePayload {
  nombreCompleto: string;
  ciudad: string;
  correo: string;
  telefono: string;
  categoria?: string;
}

export interface Producto {
  idProducto: string;
  nombre: string;
  descripcion: string;
  categoria: string;
  precioUnitario: number;
  cantidadDisponible: number;
  imagenId?: string | null;
  estado: 'activo' | 'inactivo';
  fechaRegistro: string;
  actualizadoEn: string;
}

export interface CrearProductoPayload {
  nombre: string;
  descripcion?: string;
  categoria: string;
  precioUnitario: number;
  cantidadDisponible?: number;
}

export interface ActualizarProductoPayload extends CrearProductoPayload {
  estado?: 'activo' | 'inactivo';
}

export interface DetalleFactura {
  idDetalle: number;
  idProducto: string;
  nombreProducto: string;
  categoriaProducto?: string | null;
  precioUnitario: number;
  cantidad: number;
  totalProducto: number;
  imagenId?: string | null;
}

export interface Factura {
  idFactura: number;
  numeroFactura: string;
  idCliente: string;
  clienteNombre: string;
  clienteCorreo: string;
  clienteCiudad: string;
  clienteTelefono: string;
  clienteCategoria?: string | null;
  fechaFactura: string;
  subtotal: number;
  total: number;
  metodoPago: string;
  estado: string;
  detalles: DetalleFactura[];
}

export interface ItemFacturaPayload {
  idProducto: string;
  cantidad: number;
}

export interface CrearFacturaPayload {
  idCliente: string;
  items: ItemFacturaPayload[];
}

export interface TopProductoAnalitico {
  idProducto: string;
  nombreProducto: string;
  categoriaProducto: string;
  unidadesVendidas: number;
  totalVendido: number;
}

export interface DashboardResumen {
  totalClientes: number;
  totalProductos: number;
  totalProductosActivos: number;
  totalFacturas: number;
  ventasAcumuladas: number;
  facturasRecientes: Factura[];
  productosMasVendidos: TopProductoAnalitico[];
}
