import { ToastContainer } from "react-toastify";
import Submission from "./pages/Submission";
import 'react-toastify/dist/ReactToastify.css';
import Nav from "./components/Nav";
import Credits from "./pages/Credits";
import { Route, Routes } from "react-router-dom";
import Gallery from "./pages/Gallery";

function App() {
  return (
    <>
      <ToastContainer
        position="bottom-center"
        draggable={false}
      />

      <Nav />

      <Routes>
        <Route path="/" element={<Submission />} />
        <Route path="/credits" element={<Credits />} />
        <Route path="/gallery" element={<Gallery />} />
      </Routes>
    </>
  );
}

export default App;
