import {useState} from "react";
import Tabs from "./tabs/tabs.jsx";

const TabsContainer = ({ tabs }) => {
    const [activeKey, setActiveKey] = useState(tabs[0].key);

    const activeTab = tabs.find(t => t.key === activeKey);
    const ActiveComponent = activeTab?.component;

    return (
        <div className="container-fluid px-0">
            <Tabs tabs={tabs} activeKey={activeKey} onTabChange={setActiveKey} />

            <div className="mt-3">
                {ActiveComponent && (<ActiveComponent {...activeTab.props} />)}
            </div>
        </div>
    );
};

export default TabsContainer;