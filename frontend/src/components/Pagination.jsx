// Simple prev/next pagination control for Page<T> responses.
// `page` is 0-indexed to match Spring Data's Pageable.
export default function Pagination({ page, totalPages, onChange, className = "" }) {
  if (totalPages <= 1) return null;

  return (
    <div className={`pagination ${className}`}>
      <button
        className="btn btn-ghost btn-sm"
        disabled={page <= 0}
        onClick={() => onChange(page - 1)}
      >
        قبلی
      </button>
      <span className="pagination-label">
        صفحه {page + 1} از {totalPages}
      </span>
      <button
        className="btn btn-ghost btn-sm"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        بعدی
      </button>
    </div>
  );
}
