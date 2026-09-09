import { MapPinIcon, PhoneIcon } from "./icons";

// TODO: replace with the store's real contact details.
const STORE_ADDRESS = "تهران، خیابان ولیعصر، بالاتر از میدان ونک، پلاک ۱۲۳";
const STORE_PHONES = ["021-88123456", "0912-3456789"];
const MAPS_URL = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(STORE_ADDRESS)}`;

export default function Footer() {
  return (
    <footer className="about-footer">
      <div className="container about-footer-inner">
        <div className="about-footer-brand">
          <div className="brand" style={{ marginBottom: 12 }}>
            <span className="brand-mark" />
            مارکت
          </div>
          <p>
            مارکت یک فروشگاه آنلاین تمرینیه که با هدف یادگیری توسعه‌ی بک‌اند و فرانت‌اند ساخته شده.
            محصولات متنوعی رو با قیمت مناسب و تجربه‌ی خرید ساده در اختیارت می‌ذاریم.
          </p>
        </div>

        <div className="about-footer-contact">
          <h4>راه‌های ارتباطی</h4>
          <ul>
            {STORE_PHONES.map((phone) => (
              <li key={phone}>
                <a href={`tel:${phone.replace(/[^0-9+]/g, "")}`}>
                  <PhoneIcon width={16} height={16} />
                  <span dir="ltr">{phone}</span>
                </a>
              </li>
            ))}
            <li>
              <a href={MAPS_URL} target="_blank" rel="noopener noreferrer">
                <MapPinIcon width={16} height={16} />
                <span>{STORE_ADDRESS}</span>
              </a>
            </li>
          </ul>
        </div>
      </div>

      <div className="about-footer-bottom">فروشگاه مارکت — یک پروژه‌ی تمرینی · بک‌اند Spring Boot + فرانت React</div>
    </footer>
  );
}
