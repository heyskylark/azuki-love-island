import { ToastContainer } from "react-toastify";
import Submission from "./pages/Submission";
import 'react-toastify/dist/ReactToastify.css';
import Nav from "./components/Nav";
import Credits from "./pages/Credits";
import { Route, Routes } from "react-router-dom";
import Gallery from "./pages/Gallery";
import { ProvideLatestSeason } from "./context/SeasonContext";

function App() {
  return (

    <ProvideLatestSeason>
      <ToastContainer
        position="bottom-center"
        draggable={false}
      />

      <Nav />

      <Routes>
        <Route path="/" element={<Gallery />} />
        <Route path="/gallery" element={<Gallery />} />
        <Route path="/submission" element={<Submission />} />
        <Route path="/credits" element={<Credits />} />
      </Routes>
    </ProvideLatestSeason>
  );
}

export default App;
