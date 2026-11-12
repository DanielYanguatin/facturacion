import React, { useEffect, useState } from 'react';
import {
  Package,
  Search,
  Plus,
  Edit2,
  Trash2,
  Upload,
  Database,
  Image as ImageIcon,
} from 'lucide-react';
import { productoService } from '../services/productoService';
import { getImagenUrl } from '../services/api';
import type { ActualizarProductoPayload, CrearProductoPayload, Producto } from '../types';
import { Modal } from '../components/Modal';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { AlertBanner } from '../components/AlertBanner';

export const ProductosPage: React.FC = () => {
  const [productos, setProductos] = useState<Producto[]>([]);
  const [cargando, setCargando] = useState(true);
  const [busquedaNombre, setBusquedaNombre] = useState('');
  const [categoriaFiltro, setCategoriaFiltro] = useState('');
  const [soloActivos, setSoloActivos] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [mensajeExito, setMensajeExito] = useState<string | null>(null);

  // Modales
  const [modalCrearAbierto, setModalCrearAbierto] = useState(false);
  const [modalEditarAbierto, setModalEditarAbierto] = useState(false);
  const [modalImagenAbierto, setModalImagenAbierto] = useState(false);
  const [productoSeleccionado, setProductoSeleccionado] = useState<Producto | null>(null);

  // Form State
  const [formData, setFormData] = useState<CrearProductoPayload>({
    nombre: '',
    descripcion: '',
    categoria: '',
    precioUnitario: 0,
    cantidadDisponible: 0,
  });
  const [formEstado, setFormEstado] = useState<'activo' | 'inactivo'>('activo');
  const [archivoImagen, setArchivoImagen] = useState<File | null>(null);
  const [guardando, setGuardando] = useState(false);

  const cargarProductos = async () => {
    try {
      setCargando(true);
      setError(null);
      const data = await productoService.listar({
        nombre: busquedaNombre || undefined,
        categoria: categoriaFiltro || undefined,
        soloActivos,
      });
      setProductos(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Error al consultar productos en MongoDB.');
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargarProductos();
  }, [soloActivos]);

  const handleFiltrar = (e: React.FormEvent) => {
    e.preventDefault();
    cargarProductos();
  };

  const abrirModalCrear = () => {
    setFormData({
      nombre: '',
      descripcion: '',
      categoria: '',
      precioUnitario: 0,
      cantidadDisponible: 0,
    });
    setArchivoImagen(null);
    setModalCrearAbierto(true);
  };

  const abrirModalEditar = (producto: Producto) => {
    setProductoSeleccionado(producto);
    setFormData({
      nombre: producto.nombre,
      descripcion: producto.descripcion || '',
      categoria: producto.categoria,
      precioUnitario: producto.precioUnitario,
      cantidadDisponible: producto.cantidadDisponible || 0,
    });
    setFormEstado(producto.estado);
    setModalEditarAbierto(true);
  };

  const abrirModalImagen = (producto: Producto) => {
    setProductoSeleccionado(producto);
    setArchivoImagen(null);
    setModalImagenAbierto(true);
  };

  const handleSubmitCrear = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setGuardando(true);
      setError(null);
      const nuevo = await productoService.crear(formData);
      if (archivoImagen) {
        await productoService.subirImagen(nuevo.idProducto, archivoImagen);
      }
      setMensajeExito('Producto registrado correctamente en MongoDB.');
      setModalCrearAbierto(false);
      cargarProductos();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Error al crear producto.');
    } finally {
      setGuardando(false);
    }
  };

  const handleSubmitEditar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!productoSeleccionado) return;
    try {
      setGuardando(true);
      setError(null);
      const payload: ActualizarProductoPayload = {
        ...formData,
        estado: formEstado,
      };
      await productoService.actualizar(productoSeleccionado.idProducto, payload);
      setMensajeExito('Producto actualizado en MongoDB.');
      setModalEditarAbierto(false);
      cargarProductos();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Error al actualizar producto.');
    } finally {
      setGuardando(false);
    }
  };

  const handleSubmitSubirImagen = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!productoSeleccionado || !archivoImagen) return;
    try {
      setGuardando(true);
      setError(null);
      await productoService.subirImagen(productoSeleccionado.idProducto, archivoImagen);
      setMensajeExito('Imagen almacenada con éxito en GridFS (MongoDB).');
      setModalImagenAbierto(false);
      cargarProductos();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Error al subir imagen a GridFS.');
    } finally {
      setGuardando(false);
    }
  };

  const handleDesactivar = async (idProducto: string, nombre: string) => {
    if (!window.confirm(`¿Desea desactivar el producto "${nombre}"?`)) return;
    try {
      setError(null);
      await productoService.desactivar(idProducto);
      setMensajeExito(`Producto "${nombre}" desactivado.`);
      cargarProductos();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Error al desactivar producto.');
    }
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
              Gestión de Productos
            </h1>
            <span className="px-2 py-0.5 bg-emerald-100 text-emerald-800 text-[11px] font-bold rounded flex items-center gap-1">
              <Database className="w-3 h-3" /> MongoDB + GridFS
            </span>
          </div>
          <p className="text-sm text-slate-500 mt-1">
            Almacenamiento NoSQL documental y binarios de imágenes en colección <code>productos</code> y <code>fs.files</code>.
          </p>
        </div>
        <button
          onClick={abrirModalCrear}
          className="inline-flex items-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-semibold shadow-sm transition-all cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          Registrar Producto
        </button>
      </div>

      {error && <AlertBanner tipo="error" mensaje={error} onCerrar={() => setError(null)} />}
      {mensajeExito && <AlertBanner tipo="exito" mensaje={mensajeExito} onCerrar={() => setMensajeExito(null)} />}

      {/* Search and Filters */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-col md:flex-row items-stretch md:items-center gap-3">
        <form onSubmit={handleFiltrar} className="flex-1 flex flex-col sm:flex-row items-center gap-3">
          <div className="relative flex-1 w-full">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
            <input
              type="text"
              placeholder="Buscar por nombre de producto..."
              value={busquedaNombre}
              onChange={(e) => setBusquedaNombre(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <input
            type="text"
            placeholder="Filtrar categoría..."
            value={categoriaFiltro}
            onChange={(e) => setCategoriaFiltro(e.target.value)}
            className="w-full sm:w-48 px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
          <button
            type="submit"
            className="w-full sm:w-auto px-4 py-2 bg-slate-800 hover:bg-slate-900 text-white text-sm font-medium rounded-lg transition-colors cursor-pointer"
          >
            Filtrar
          </button>
        </form>

        <div className="flex items-center gap-2 pl-2 border-t md:border-t-0 md:border-l border-slate-200 pt-2 md:pt-0">
          <label className="flex items-center gap-2 text-xs font-semibold text-slate-600 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={soloActivos}
              onChange={(e) => setSoloActivos(e.target.checked)}
              className="rounded text-indigo-600 focus:ring-indigo-500"
            />
            Solo activos
          </label>
        </div>
      </div>

      {/* Grid of Products */}
      {cargando ? (
        <LoadingSpinner texto="Consultando catálogo en MongoDB..." />
      ) : productos.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {productos.map((prod) => (
            <div
              key={prod.idProducto}
              className={`bg-white rounded-2xl border shadow-xs overflow-hidden flex flex-col transition-all hover:shadow-md ${
                prod.estado === 'activo' ? 'border-slate-200' : 'border-slate-300 opacity-75 bg-slate-50/50'
              }`}
            >
              {/* Product Image */}
              <div className="h-44 bg-slate-100 relative overflow-hidden flex items-center justify-center group">
                {prod.imagenId ? (
                  <img
                    src={getImagenUrl(prod.idProducto)}
                    alt={prod.nombre}
                    className="w-full h-full object-cover transition-transform group-hover:scale-105 duration-300"
                    onError={(e) => {
                      (e.target as HTMLElement).style.display = 'none';
                    }}
                  />
                ) : (
                  <div className="flex flex-col items-center justify-center text-slate-400">
                    <ImageIcon className="w-10 h-10 stroke-1 mb-1 text-slate-300" />
                    <span className="text-xs">Sin imagen en GridFS</span>
                  </div>
                )}
                <span
                  className={`absolute top-3 right-3 px-2 py-0.5 rounded-full text-[11px] font-bold shadow-xs ${
                    prod.estado === 'activo'
                      ? 'bg-emerald-500 text-white'
                      : 'bg-rose-500 text-white'
                  }`}
                >
                  {prod.estado === 'activo' ? 'Activo' : 'Inactivo'}
                </span>
                <span className="absolute bottom-3 left-3 px-2.5 py-0.5 rounded-md text-[11px] font-semibold bg-slate-900/80 text-white backdrop-blur-xs">
                  {prod.categoria}
                </span>
              </div>

              {/* Body */}
              <div className="p-5 flex-1 flex flex-col justify-between space-y-4">
                <div>
                  <h3 className="font-bold text-base text-slate-900 line-clamp-1">
                    {prod.nombre}
                  </h3>
                  <p className="text-xs text-slate-500 mt-1 line-clamp-2 min-h-8">
                    {prod.descripcion || 'Sin descripción detallada.'}
                  </p>
                </div>

                <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
                  <div>
                    <span className="text-[11px] uppercase font-semibold text-slate-400 block">
                      Precio Unitario
                    </span>
                    <span className="text-lg font-bold text-indigo-700">
                      $ {prod.precioUnitario.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
                    </span>
                  </div>
                  <div className="text-right">
                    <span className="text-[11px] uppercase font-semibold text-slate-400 block">
                      Stock (Info)
                    </span>
                    <span className="text-sm font-semibold text-slate-700">
                      {prod.cantidadDisponible ?? 0} u.
                    </span>
                  </div>
                </div>

                {/* Card Actions */}
                <div className="pt-2 border-t border-slate-100 flex items-center justify-between gap-2">
                  <button
                    onClick={() => abrirModalImagen(prod)}
                    title="Subir/Cambiar imagen en GridFS"
                    className="flex-1 py-1.5 px-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg text-xs font-semibold flex items-center justify-center gap-1 transition-colors cursor-pointer"
                  >
                    <Upload className="w-3.5 h-3.5" /> Imagen
                  </button>
                  <button
                    onClick={() => abrirModalEditar(prod)}
                    title="Editar datos"
                    className="p-1.5 text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors cursor-pointer"
                  >
                    <Edit2 className="w-4 h-4" />
                  </button>
                  {prod.estado === 'activo' && (
                    <button
                      onClick={() => handleDesactivar(prod.idProducto, prod.nombre)}
                      title="Desactivar producto"
                      className="p-1.5 text-rose-500 hover:bg-rose-50 rounded-lg transition-colors cursor-pointer"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 p-16 text-center text-slate-400 space-y-2">
          <Package className="w-12 h-12 mx-auto text-slate-300" />
          <p className="font-semibold text-slate-600">No se encontraron productos en MongoDB</p>
          <p className="text-xs">Registra un nuevo producto con su precio e imagen para comenzar.</p>
        </div>
      )}

      {/* Modal Crear Producto */}
      <Modal
        isOpen={modalCrearAbierto}
        onClose={() => setModalCrearAbierto(false)}
        titulo="Registrar Nuevo Producto en MongoDB"
      >
        <form onSubmit={handleSubmitCrear} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Nombre del Producto *
            </label>
            <input
              type="text"
              required
              value={formData.nombre}
              onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
              placeholder="Ej. Teclado Mecánico RGB"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Descripción
            </label>
            <textarea
              rows={2}
              value={formData.descripcion}
              onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
              placeholder="Características destacadas del producto..."
            />
          </div>

          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                Categoría *
              </label>
              <input
                type="text"
                required
                value={formData.categoria}
                onChange={(e) => setFormData({ ...formData, categoria: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
                placeholder="Ej. Periféricos"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                Precio Unitario ($) *
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                required
                value={formData.precioUnitario}
                onChange={(e) => setFormData({ ...formData, precioUnitario: parseFloat(e.target.value) || 0 })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                Cantidad Disp. (Info)
              </label>
              <input
                type="number"
                min="0"
                value={formData.cantidadDisponible}
                onChange={(e) => setFormData({ ...formData, cantidadDisponible: parseInt(e.target.value, 10) || 0 })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Imagen del Producto (Guardada en GridFS)
            </label>
            <input
              type="file"
              accept="image/*"
              onChange={(e) => setArchivoImagen(e.target.files?.[0] || null)}
              className="w-full text-xs text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100 cursor-pointer"
            />
          </div>

          <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={() => setModalCrearAbierto(false)}
              className="px-4 py-2 border border-slate-300 text-slate-700 hover:bg-slate-50 rounded-lg text-sm font-medium cursor-pointer"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={guardando}
              className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-semibold shadow-xs disabled:opacity-50 cursor-pointer"
            >
              {guardando ? 'Guardando...' : 'Guardar Producto'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Modal Editar Producto */}
      <Modal
        isOpen={modalEditarAbierto}
        onClose={() => setModalEditarAbierto(false)}
        titulo="Editar Producto en MongoDB"
      >
        <form onSubmit={handleSubmitEditar} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Nombre del Producto *
            </label>
            <input
              type="text"
              required
              value={formData.nombre}
              onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Descripción
            </label>
            <textarea
              rows={2}
              value={formData.descripcion}
              onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                Categoría *
              </label>
              <input
                type="text"
                required
                value={formData.categoria}
                onChange={(e) => setFormData({ ...formData, categoria: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                Precio Unitario ($) *
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                required
                value={formData.precioUnitario}
                onChange={(e) => setFormData({ ...formData, precioUnitario: parseFloat(e.target.value) || 0 })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
                Cantidad Disp.
              </label>
              <input
                type="number"
                min="0"
                value={formData.cantidadDisponible}
                onChange={(e) => setFormData({ ...formData, cantidadDisponible: parseInt(e.target.value, 10) || 0 })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 uppercase mb-1">
              Estado
            </label>
            <select
              value={formEstado}
              onChange={(e) => setFormEstado(e.target.value as 'activo' | 'inactivo')}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500"
            >
              <option value="activo">Activo (Disponible para facturación)</option>
              <option value="inactivo">Inactivo (No facturable)</option>
            </select>
          </div>

          <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={() => setModalEditarAbierto(false)}
              className="px-4 py-2 border border-slate-300 text-slate-700 hover:bg-slate-50 rounded-lg text-sm font-medium cursor-pointer"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={guardando}
              className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-semibold shadow-xs disabled:opacity-50 cursor-pointer"
            >
              {guardando ? 'Actualizando...' : 'Actualizar Producto'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Modal Subir Imagen GridFS */}
      <Modal
        isOpen={modalImagenAbierto}
        onClose={() => setModalImagenAbierto(false)}
        titulo={`Subir Imagen a GridFS - ${productoSeleccionado?.nombre}`}
      >
        <form onSubmit={handleSubmitSubirImagen} className="space-y-4">
          <p className="text-xs text-slate-500">
            La imagen será persistida como binario segmentado en las colecciones <code>fs.files</code> y <code>fs.chunks</code> de MongoDB.
          </p>
          <div>
            <input
              type="file"
              required
              accept="image/*"
              onChange={(e) => setArchivoImagen(e.target.files?.[0] || null)}
              className="w-full text-xs text-slate-500 file:mr-4 file:py-2.5 file:px-4 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100 cursor-pointer"
            />
          </div>
          <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={() => setModalImagenAbierto(false)}
              className="px-4 py-2 border border-slate-300 text-slate-700 hover:bg-slate-50 rounded-lg text-sm font-medium cursor-pointer"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={guardando || !archivoImagen}
              className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-semibold shadow-xs disabled:opacity-50 cursor-pointer"
            >
              {guardando ? 'Guardando en GridFS...' : 'Subir Imagen'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
