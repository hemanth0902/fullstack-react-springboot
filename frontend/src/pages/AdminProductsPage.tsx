
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useProducts, useDeleteProduct } from '../hooks/useProducts'
import ProductCard from '../components/product/ProductCard'
import LoadingSpinner from '../components/common/LoadingSpinner'

export default function AdminProductsPage() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useProducts(page, 9)
  const deleteProduct = useDeleteProduct()

  const handleDelete = (id: number) => {
    if (window.confirm('Are you sure you want to delete this product?')) {
      deleteProduct.mutate(id)
    }
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Page heading and action buttons */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Product Management
          </h1>
          <p className="text-gray-500 mt-1">
            {data?.totalElements ?? 0} total products
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <Link
            to="/products"
            className="inline-flex items-center justify-center rounded-lg bg-blue-600 px-5 py-2.5 font-medium text-white shadow-sm transition-colors hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            Browse Products
          </Link>

          <Link
            to="/admin/products/new"
            className="inline-flex items-center justify-center rounded-lg bg-blue-600 px-5 py-2.5 font-medium text-white shadow-sm transition-colors hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            + Add Product
          </Link>
        </div>
      </div>

      {/* Product list */}
      {isLoading ? (
        <LoadingSpinner />
      ) : (
        <>
          {data?.content && data.content.length > 0 ? (
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {data.content.map((product) => (
                <ProductCard
                  key={product.id}
                  product={product}
                  isAdmin
                  onDelete={handleDelete}
                />
              ))}
            </div>
          ) : (
            <div className="rounded-xl border border-gray-200 bg-white py-12 text-center shadow-sm">
              <p className="text-lg font-medium text-gray-700">
                No products found
              </p>
              <p className="mt-2 text-gray-500">
                Add a product to get started.
              </p>
              <Link
                to="/admin/products/new"
                className="mt-5 inline-flex items-center justify-center rounded-lg bg-blue-600 px-5 py-2.5 font-medium text-white transition-colors hover:bg-blue-700"
              >
                + Add Product
              </Link>
            </div>
          )}

          {/* Pagination */}
          {data && data.totalPages > 1 && (
            <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
              <button
                type="button"
                disabled={page === 0}
                onClick={() => setPage((current) => current - 1)}
                className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-gray-700 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
              >
                Previous
              </button>

              <span className="px-2 py-2 text-gray-600">
                Page {page + 1} of {data.totalPages}
              </span>

              <button
                type="button"
                disabled={data.last}
                onClick={() => setPage((current) => current + 1)}
                className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-gray-700 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
