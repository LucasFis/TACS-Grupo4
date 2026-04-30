const Tabs = ({ tabs, activeKey, onTabChange }) => (
    <ul className="nav nav-tabs">
        {tabs.map(({ key, label }) => (
            <li key={key} className="nav-item">
                <button
                    className={`nav-link ${activeKey === key ? 'active' : ''}`}
                    onClick={() => onTabChange(key)}
                >
                    {label}
                </button>
            </li>
        ))}
    </ul>
);

export default Tabs